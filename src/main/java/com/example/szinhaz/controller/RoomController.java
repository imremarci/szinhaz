package com.example.szinhaz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {

    @GetMapping("/termek")
    public String listRooms() {
        return "redirect:/fooldal";
    }
}