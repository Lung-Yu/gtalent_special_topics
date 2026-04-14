package com.gtalent.helloworld.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.gtalent.helloworld.service.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {


    

}
