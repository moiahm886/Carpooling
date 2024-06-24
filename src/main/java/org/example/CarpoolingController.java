package org.example;

import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carpooling")
public class CarpoolingController {

    @Autowired
    private CarpoolingService carpoolingService;

    @PostMapping("/init")
    public String initCarpooling(@RequestParam String carpoolingId, @RequestParam String owner,
                                 @RequestParam String origin, @RequestParam String destination,
                                 @RequestParam int nSlot, @RequestParam int price, @RequestParam long startTime) {
        try {
            return carpoolingService.initCarpooling(carpoolingId, owner, origin, destination, nSlot, price, startTime);
        } catch (InvalidArgumentException | ProposalException e) {
            return "Error initializing carpooling: " + e.getMessage();
        }
    }

    @PostMapping("/book")
    public String bookSlots(@RequestParam String carpoolingId, @RequestParam String user,
                            @RequestParam int nSlotBooked, @RequestParam int amount) {
        try {
            return carpoolingService.bookSlots(carpoolingId, user, nSlotBooked, amount);
        } catch (InvalidArgumentException | ProposalException e) {
            return "Error booking slots: " + e.getMessage();
        }
    }

    @PostMapping("/refund")
    public String requestRefund(@RequestParam String carpoolingId, @RequestParam String user,
                                @RequestParam int nSlotAskRefund) {
        try {
            return carpoolingService.requestRefund(carpoolingId, user, nSlotAskRefund);
        } catch (InvalidArgumentException | ProposalException e) {
            return "Error requesting refund: " + e.getMessage();
        }
    }

    @PostMapping("/validate")
    public String validateBooking(@RequestParam String carpoolingId, @RequestParam String owner) {
        try {
            return carpoolingService.validateBooking(carpoolingId, owner);
        } catch (InvalidArgumentException | ProposalException e) {
            return "Error validating booking: " + e.getMessage();
        }
    }

    @PostMapping("/settle")
    public String settleBooking(@RequestParam String carpoolingId, @RequestParam String from,
                                @RequestParam String to, @RequestParam int amountRefunded) {
        try {
            return carpoolingService.settleBooking(carpoolingId, from, to, amountRefunded);
        } catch (InvalidArgumentException | ProposalException e) {
            return "Error settling booking: " + e.getMessage();
        }
    }
}
