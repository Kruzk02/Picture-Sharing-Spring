package com.app.Controller;

import com.app.Model.Pin;
import com.app.Service.PinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    private final PinService pinService;

    @Autowired
    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins(){
        try {
            List<Pin> pins = pinService.getAllPins();
            return ResponseEntity.ok(pins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(){

    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(){

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id){
        try{
            Pin pin = pinService.findById(id);
            return ResponseEntity.ok(pin);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePinById(@PathVariable Long id){
        try{
            pinService.deleteById(id);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
