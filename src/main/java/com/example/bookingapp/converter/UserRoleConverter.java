//package com.example.bookingapp.converter;
//
//import com.example.cruddemo.entity.User.Role;
//import jakarta.persistence.AttributeConverter;
//
//public class  UserRoleConverter implements AttributeConverter<Role, Character> {
//
//
//    // convert từ java entity to databasse
//    @Override
//    public Character convertToDatabaseColumn(Role role) {
//        return role.toString().charAt(0);
//    }
//
//    // convert từ database sang entity
//    @Override
//    public Role convertToEntityAttribute(Character code) {
//        if(code == 'A') return Role.ADMIN;
//        if(code == 'E') return Role.EMPLOYEE;
//        return Role.MANAGER;
//    }
//}
