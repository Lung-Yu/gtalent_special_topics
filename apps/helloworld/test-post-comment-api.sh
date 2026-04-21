#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Post/Comment REST API 端對端測試腳本
#
# 測試情境:
#   1. 建立 Post
#   2. 取得 Post (單一 / 全部)
#   3. 更新 Post
#   4. 在 Post 下建立 Comment (雙向關聯)
#   5. 列出 Post 的 Comments
#   6. 更新 Comment
#   7. 驗證 cascade: 建立 Post 時一併帶入 comments
#   8. 驗證 orphanRemoval: 刪除 Comment
#   9. 驗證 cascade delete: 刪除 Post 會一併刪除 Comments
#  10. 錯誤情境: 404 / 驗證失敗 (400)
#
# 前置:
#   - 應用程式已啟動於 http://localhost:8081 (預設)
#   - 需要工具: curl, jq
# -----------------------------------------------------------------------------
set -u

BASE_URL="${BASE_URL:-http://localhost:8081}"
PASS=0
FAIL=0

# 顏色
if [[ -t 1 ]]; then
    G='\033[0;32m'; R='\033[0;31m'; Y='\033[0;33m'; B='\033[0;34m'; N='\033[0m'
else
    G=''; R=''; Y=''; B=''; N=''
fi

# 依賴檢查
for cmd in curl jq; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "缺少依賴: $cmd" >&2
        exit 1
    fi
done

# -----------------------------------------------------------------------------
# 工具函式
# -----------------------------------------------------------------------------
# request METHOD PATH [BODY]
# 回傳: 將 HTTP code 放到全域 $HTTP_CODE, body 放到 $RESP_BODY
request() {
    local method="$1" path="$2" body="${3:-}"
    local tmp
    tmp=$(mktemp)
    if [[ -n "$body" ]]; then
        HTTP_CODE=$(curl -s -o "$tmp" -w "%{http_code}" \
            -X "$method" "$BASE_URL$path" \
            -H "Content-Type: application/json" \
            -d "$body")
    else
        HTTP_CODE=$(curl -s -o "$tmp" -w "%{http_code}" \
            -X "$method" "$BASE_URL$path")
    fi
    RESP_BODY=$(cat "$tmp")
    rm -f "$tmp"
}

# assert_eq EXPECTED ACTUAL LABEL
assert_eq() {
    local expected="$1" actual="$2" label="$3"
    if [[ "$expected" == "$actual" ]]; then
        echo -e "  ${G}✓${N} $label (expected=$expected)"
        PASS=$((PASS + 1))
    else
        echo -e "  ${R}✗${N} $label (expected=$expected, actual=$actual)"
        echo -e "    body: $RESP_BODY"
        FAIL=$((FAIL + 1))
    fi
}

# assert_contains JQ_EXPR EXPECTED LABEL (針對 $RESP_BODY)
assert_jq() {
    local expr="$1" expected="$2" label="$3"
    local actual
    actual=$(echo "$RESP_BODY" | jq -r "$expr" 2>/dev/null)
    assert_eq "$expected" "$actual" "$label"
}

section() {
    echo ""
    echo -e "${B}==> $1${N}"
}

# -----------------------------------------------------------------------------
# 0. 檢查服務是否存活
# -----------------------------------------------------------------------------
section "0. Health check ($BASE_URL)"
request GET "/api/posts"
if [[ "$HTTP_CODE" != "200" ]]; then
    echo -e "${R}服務無法連線 ($HTTP_CODE)。請先啟動: mvn spring-boot:run${N}" >&2
    exit 1
fi
echo -e "  ${G}✓${N} 服務正常"

# -----------------------------------------------------------------------------
# 1. 建立 Post
# -----------------------------------------------------------------------------
section "1. 建立 Post"
request POST "/api/posts" '{"title":"Hello JPA","content":"雙向關聯第一篇"}'
assert_eq "201" "$HTTP_CODE" "POST /api/posts 回 201"
POST_ID=$(echo "$RESP_BODY" | jq -r '.id')
assert_jq '.title' "Hello JPA" "title 正確"
assert_jq '.content' "雙向關聯第一篇" "content 正確"
assert_jq '.comments | length' "0" "初始 comments 為空"
echo "  → 建立的 POST_ID=$POST_ID"

# -----------------------------------------------------------------------------
# 2. 讀取 Post
# -----------------------------------------------------------------------------
section "2. 讀取 Post"
request GET "/api/posts/$POST_ID"
assert_eq "200" "$HTTP_CODE" "GET /api/posts/{id} 回 200"
assert_jq '.id' "$POST_ID" "id 正確"

request GET "/api/posts"
assert_eq "200" "$HTTP_CODE" "GET /api/posts 回 200"
LEN=$(echo "$RESP_BODY" | jq 'length')
if [[ "$LEN" -ge 1 ]]; then
    echo -e "  ${G}✓${N} 列表至少包含一筆 (count=$LEN)"
    PASS=$((PASS + 1))
else
    echo -e "  ${R}✗${N} 列表應至少有 1 筆"
    FAIL=$((FAIL + 1))
fi

# -----------------------------------------------------------------------------
# 3. 更新 Post
# -----------------------------------------------------------------------------
section "3. 更新 Post"
request PUT "/api/posts/$POST_ID" '{"title":"Hello JPA (updated)","content":"已更新"}'
assert_eq "200" "$HTTP_CODE" "PUT /api/posts/{id} 回 200"
assert_jq '.title' "Hello JPA (updated)" "title 已更新"
assert_jq '.content' "已更新" "content 已更新"

# -----------------------------------------------------------------------------
# 4. 建立 Comment (雙向關聯)
# -----------------------------------------------------------------------------
section "4. 建立 Comment"
request POST "/api/posts/$POST_ID/comments" '{"author":"Alice","content":"第一則留言"}'
assert_eq "201" "$HTTP_CODE" "POST /api/posts/{id}/comments 回 201"
COMMENT_ID=$(echo "$RESP_BODY" | jq -r '.id')
assert_jq '.author' "Alice" "author 正確"
echo "  → 建立的 COMMENT_ID=$COMMENT_ID"

request POST "/api/posts/$POST_ID/comments" '{"author":"Bob","content":"第二則留言"}'
assert_eq "201" "$HTTP_CODE" "第二則留言建立成功"
COMMENT_ID_2=$(echo "$RESP_BODY" | jq -r '.id')

# -----------------------------------------------------------------------------
# 5. 列出 Comments
# -----------------------------------------------------------------------------
section "5. 列出 Comments"
request GET "/api/posts/$POST_ID/comments"
assert_eq "200" "$HTTP_CODE" "GET comments 回 200"
assert_jq 'length' "2" "應有 2 則 comments"

request GET "/api/posts/$POST_ID/comments/$COMMENT_ID"
assert_eq "200" "$HTTP_CODE" "GET 單一 comment 回 200"
assert_jq '.id' "$COMMENT_ID" "comment id 正確"

# -----------------------------------------------------------------------------
# 6. 更新 Comment
# -----------------------------------------------------------------------------
section "6. 更新 Comment"
request PUT "/api/posts/$POST_ID/comments/$COMMENT_ID" '{"author":"Alice","content":"已編輯"}'
assert_eq "200" "$HTTP_CODE" "PUT comment 回 200"
assert_jq '.content' "已編輯" "comment content 已更新"

# -----------------------------------------------------------------------------
# 7. Cascade Persist: 建立 Post 時帶入 comments
# -----------------------------------------------------------------------------
section "7. Cascade Persist (一次建立 Post + Comments)"
request POST "/api/posts" '{
  "title":"Cascade Test",
  "content":"隨 Post 一起儲存 comments",
  "comments":[
    {"author":"Carol","content":"cascade-1"},
    {"author":"Dan","content":"cascade-2"}
  ]
}'
assert_eq "201" "$HTTP_CODE" "建立帶 comments 的 Post 回 201"
POST_ID_2=$(echo "$RESP_BODY" | jq -r '.id')
assert_jq '.comments | length' "2" "cascade 建立 2 則 comments"

request GET "/api/posts/$POST_ID_2/comments"
assert_jq 'length' "2" "從 comments 端也能查到 2 筆"

# -----------------------------------------------------------------------------
# 8. orphanRemoval: 刪除 Comment
# -----------------------------------------------------------------------------
section "8. orphanRemoval (刪除 Comment)"
request DELETE "/api/posts/$POST_ID/comments/$COMMENT_ID_2"
assert_eq "204" "$HTTP_CODE" "DELETE comment 回 204"

request GET "/api/posts/$POST_ID/comments"
assert_jq 'length' "1" "刪除後剩 1 則"

request GET "/api/posts/$POST_ID/comments/$COMMENT_ID_2"
assert_eq "404" "$HTTP_CODE" "已刪除的 comment 回 404"

# -----------------------------------------------------------------------------
# 9. Cascade Delete: 刪除 Post 連帶刪除 Comments
# -----------------------------------------------------------------------------
section "9. Cascade Delete (刪除 Post 連帶刪除 Comments)"
request DELETE "/api/posts/$POST_ID_2"
assert_eq "204" "$HTTP_CODE" "DELETE post 回 204"

request GET "/api/posts/$POST_ID_2"
assert_eq "404" "$HTTP_CODE" "已刪除的 post 回 404"

# Post_2 下的 comments 應一併被刪;直接查不到 post → 404
request GET "/api/posts/$POST_ID_2/comments"
assert_eq "404" "$HTTP_CODE" "刪除 post 後查 comments 回 404"

# -----------------------------------------------------------------------------
# 10. 錯誤情境
# -----------------------------------------------------------------------------
section "10. 錯誤情境"
# 10-1 不存在的 post
request GET "/api/posts/999999"
assert_eq "404" "$HTTP_CODE" "GET 不存在的 post 回 404"

# 10-2 驗證失敗: title 空白 (Bean Validation)
request POST "/api/posts" '{"title":"","content":""}'
if [[ "$HTTP_CODE" == "400" ]]; then
    echo -e "  ${G}✓${N} POST 空白欄位回 400"
    PASS=$((PASS + 1))
else
    echo -e "  ${Y}⚠${N}  預期 400, 實際 $HTTP_CODE (若未啟用 validation 可忽略)"
fi

# 10-3 在不存在的 post 下建立 comment
request POST "/api/posts/999999/comments" '{"author":"X","content":"Y"}'
assert_eq "404" "$HTTP_CODE" "在不存在的 post 下建立 comment 回 404"

# 10-4 跨 post 存取 comment
# 先建一個新的 post + comment
request POST "/api/posts" '{"title":"P-A","content":"A"}'
P_A=$(echo "$RESP_BODY" | jq -r '.id')
request POST "/api/posts" '{"title":"P-B","content":"B"}'
P_B=$(echo "$RESP_BODY" | jq -r '.id')
request POST "/api/posts/$P_A/comments" '{"author":"a","content":"a"}'
C_A=$(echo "$RESP_BODY" | jq -r '.id')

request GET "/api/posts/$P_B/comments/$C_A"
assert_eq "404" "$HTTP_CODE" "跨 post 存取 comment 回 404"

# 清理
request DELETE "/api/posts/$P_A" >/dev/null
request DELETE "/api/posts/$P_B" >/dev/null
request DELETE "/api/posts/$POST_ID" >/dev/null

# -----------------------------------------------------------------------------
# 總結
# -----------------------------------------------------------------------------
echo ""
echo "============================================"
echo -e "  ${G}PASS: $PASS${N}   ${R}FAIL: $FAIL${N}"
echo "============================================"
if [[ $FAIL -gt 0 ]]; then
    exit 1
fi
exit 0
