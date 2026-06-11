package com.gtalent.helloworld.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gtalent.helloworld.config.CacheConfig;
import com.gtalent.helloworld.controller.resp.CategoryResp;
import com.gtalent.helloworld.domain.valueobject.TypeCategory;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CategoryCacheService {

    private static final String KEY_PREFIX = "v2::";

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    public CategoryCacheService(CacheManager cacheManager, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
    }

    public Page<CategoryResp> getCategoryList(TypeCategory type,
                                              Pageable pageable,
                                              Supplier<Page<CategoryResp>> dbLoader) {
        String key = buildListKey(type, pageable);
        CachedCategoryPage cachedPage = getOrLoad(key, "list",
                () -> CachedCategoryPage.from(dbLoader.get()));
        return cachedPage.toPage(pageable);
    }

    public CategoryResp getCategoryById(Long id, Supplier<CategoryResp> dbLoader) {
        String key = KEY_PREFIX + "id::" + id;
        return getOrLoad(key, "item", dbLoader);
    }

    public void evictAll() {
        Cache cache = getCategoriesCache();
        cache.clear();
        meterRegistry.counter("app_cache_evictions_total", "cache", CacheConfig.CATEGORIES_CACHE)
                .increment();
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrLoad(String key, String operation, Supplier<T> dbLoader) {
        Cache cache = getCategoriesCache();
        meterRegistry.counter("app_cache_requests_total",
                "cache", CacheConfig.CATEGORIES_CACHE,
                "operation", operation,
                "result", "total").increment();

        Cache.ValueWrapper cached = cache.get(key);
        if (cached != null) {
            meterRegistry.counter("app_cache_requests_total",
                    "cache", CacheConfig.CATEGORIES_CACHE,
                    "operation", operation,
                    "result", "hit").increment();
            return (T) cached.get();
        }

        meterRegistry.counter("app_cache_requests_total",
                "cache", CacheConfig.CATEGORIES_CACHE,
                "operation", operation,
                "result", "miss").increment();

        T loaded = dbLoader.get();
        cache.put(key, loaded);
        return loaded;
    }

    private String buildListKey(TypeCategory type, Pageable pageable) {
        String typeValue = (type == null) ? "ALL" : type.name();
        String sortValue = pageable.getSort().isSorted() ? pageable.getSort().toString() : "unsorted";
        return KEY_PREFIX + "list::type=" + typeValue
                + "::page=" + pageable.getPageNumber()
                + "::size=" + pageable.getPageSize()
                + "::sort=" + sortValue;
    }

    private Cache getCategoriesCache() {
        Cache cache = cacheManager.getCache(CacheConfig.CATEGORIES_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + CacheConfig.CATEGORIES_CACHE);
        }
        return cache;
    }

    public static class CachedCategoryPage {
        private List<CategoryResp> content = new ArrayList<>();
        private long totalElements;

        public static CachedCategoryPage from(Page<CategoryResp> page) {
            CachedCategoryPage cached = new CachedCategoryPage();
            cached.setContent(new ArrayList<>(page.getContent()));
            cached.setTotalElements(page.getTotalElements());
            return cached;
        }

        public Page<CategoryResp> toPage(Pageable pageable) {
            return new PageImpl<>(content, pageable, totalElements);
        }

        public List<CategoryResp> getContent() {
            return content;
        }

        public void setContent(List<CategoryResp> content) {
            this.content = (content == null) ? new ArrayList<>() : content;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
    }
}
