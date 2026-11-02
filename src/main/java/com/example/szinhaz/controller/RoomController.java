package com.example.szinhaz.controller;

import com.example.szinhaz.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/termek")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "termek"; // Későbbi termek.html
    }
}