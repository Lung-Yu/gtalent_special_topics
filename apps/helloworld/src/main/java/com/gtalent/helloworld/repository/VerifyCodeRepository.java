package com.gtalent.helloworld.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.gtalent.helloworld.service.entities.VerifyCode;


public interface VerifyCodeRepository extends CrudRepository<VerifyCode, Long> {

    /** 取得指定 serviceId 最新產生的 secret */
    Optional<VerifyCode> findTopByServiceIdOrderByLastUpdatedAtDesc(String serviceId);
}
