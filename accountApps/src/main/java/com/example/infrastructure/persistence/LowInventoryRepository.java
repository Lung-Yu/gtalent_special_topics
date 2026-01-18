package com.example.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;

import com.example.domain.model.Inventory;
import com.example.domain.repository.InventoryRepository;

public class LowInventoryRepository implements InventoryRepository {

    private List<Inventory> list;

    public LowInventoryRepository(){
        this.list = new ArrayList<>();
    }


    @Override
    public Inventory findById(String id) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getId().equals(id)){
                return list.get(i);
            }
        }

        return null;
    }

    @Override
    public void save(Inventory inventory) {
        list.add(inventory);
    }

}
