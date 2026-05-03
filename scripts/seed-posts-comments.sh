#!/usr/bin/env bash
# 建立 30 個 Post，以及 50 個 Comment 隨機分散在這 30 個 Post 底下
#
# 使用方式:
#   ./scripts/seed-posts-comments.sh                     # 預設 http://localhost:8080
#   BASE_URL=http://localhost:9090 ./scripts/seed-posts-comments.sh
#
# 需求: curl, jq

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8880}"
POST_COUNT="${POST_COUNT:-30}"
COMMENT_COUNT="${COMMENT_COUNT:-50}"

command -v curl >/dev/null || { echo "需要安裝 curl"; exit 1; }
command -v jq   >/dev/null || { echo "需要安裝 jq";   exit 1; }

echo "==> Target: $BASE_URL"
echo "==> 建立 $POST_COUNT 個 Post..."

post_ids=()
for i in $(seq 1 "$POST_COUNT"); do
  title="測試貼文 #$i"
  content="這是第 $i 篇自動產生的貼文內容，用於測試 API。"
  body=$(jq -n --arg t "$title" --arg c "$content" '{title:$t, content:$c}')

  resp=$(curl -sS -X POST "$BASE_URL/api/posts" \
    -H "Content-Type: application/json" \
    -d "$body")

  id=$(echo "$resp" | jq -r '.id')
  if [[ -z "$id" || "$id" == "null" ]]; then
    echo "建立 Post 失敗: $resp" >&2
    exit 1
  fi
  post_ids+=("$id")
  echo "  + Post id=$id  title=$title"
done

echo "==> 建立 $COMMENT_COUNT 個 Comment (隨機分散至以上 Post)..."

authors=("Alice" "Bob" "Charlie" "David" "Eve" "Frank" "Grace" "Henry" "Ivy" "Jack")

for j in $(seq 1 "$COMMENT_COUNT"); do
  # 從 post_ids 中隨機取一個
  idx=$((RANDOM % ${#post_ids[@]}))
  post_id=${post_ids[$idx]}

  author=${authors[$((RANDOM % ${#authors[@]}))]}
  content="隨機留言 #$j by $author"
  body=$(jq -n --arg a "$author" --arg c "$content" '{author:$a, content:$c}')

  resp=$(curl -sS -X POST "$BASE_URL/api/posts/$post_id/comments" \
    -H "Content-Type: application/json" \
    -d "$body")

  cid=$(echo "$resp" | jq -r '.id')
  if [[ -z "$cid" || "$cid" == "null" ]]; then
    echo "建立 Comment 失敗 (postId=$post_id): $resp" >&2
    exit 1
  fi
  echo "  + Comment id=$cid -> Post id=$post_id  author=$author"
done

echo "==> 完成: $POST_COUNT posts, $COMMENT_COUNT comments"
