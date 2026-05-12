package com.gtalent.helloworld.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gtalent.helloworld.controller.req.CategoryCreateReq;
import com.gtalent.helloworld.controller.resp.CategoryResp;
import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.valueobject.TypeCategory;
import com.gtalent.helloworld.service.CategoryService;
import com.gtalent.helloworld.service.UserRepository;
import com.gtalent.helloworld.service.entities.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    /** GET /api/categories?type=INCOME&page=0&size=20 */
    @GetMapping
    public Page<CategoryResp> findAll(
            @RequestParam(required = false) TypeCategory type,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        Page<Category> categories = (type != null)
                ? categoryService.findByType(type, pageable)
                : categoryService.findAll(pageable);

        return categories.map(CategoryResp::from);
    }

    /** GET /api/categories/{id} */
    @GetMapping("/{id}")
    public CategoryResp findOne(@PathVariable Long id) {
        return CategoryResp.from(categoryService.findById(id));
    }

    /** POST /api/categories */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResp create(@Valid @RequestBody CategoryCreateReq req, Authentication auth) {
        User currentUser = resolveUser(auth);
        Category category = categoryService.create(req.getName(), req.getIcon(), req.getType(), currentUser);
        return CategoryResp.from(category);
    }

    /** PUT /api/categories/{id} */
    @PutMapping("/{id}")
    public CategoryResp update(@PathVariable Long id, @RequestBody CategoryCreateReq req) {
        Category updated = categoryService.update(id, req.getName(), req.getIcon());
        return CategoryResp.from(updated);
    }

    /** DELETE /api/categories/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登入");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));
    }
}
