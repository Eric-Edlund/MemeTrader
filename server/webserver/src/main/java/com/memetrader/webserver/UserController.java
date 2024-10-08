package com.memetrader.webserver;

import com.memetrader.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private MemeStockService memeStockService;
    @Autowired
    private MemeStockRepository memeStockRepository;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<UserMetadataV1> userMetadata(@AuthenticationPrincipal StockUser user) {
        return ResponseEntity.ok(memeStockRepository.getUserMetadata(user.getUserId()));
    }

    @PostMapping(value = "/order", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Object> placeOrder(@RequestBody OrderRequestForm form,
            @RequestParam(value = "dryRun", required = false, defaultValue = "false") boolean dryRun,
            @RequestParam(value = "getTotalPrice", required = false, defaultValue = "false") boolean getTotal) {
        if (getTotal) {
            var total = memeStockService.getTransactionValue(form.stockId(), form.operation(), form.numShares());
            System.out.println("Requesting total " + total);
            return ResponseEntity.ok(total);
        }

        if (form.numShares() == 0) {
            return ResponseEntity.badRequest().body("numShares cannot be zero");
        }

        if (dryRun) {
            System.out.println("Dry run order");
        }
        try {
            memeStockService.placeOrder(form.userId(), form.stockId(), form.order(), form.numShares(),
                    form.totalPrice(), dryRun);
            if (dryRun) {
                return ResponseEntity.ok(new TransactionDryRunStatus(true, null));
            }
            System.out.println(form);
        } catch (StockOrderException e) {
            if (dryRun) {
                return ResponseEntity.ok(new TransactionDryRunStatus(false, e.problem));
            }

            return ResponseEntity.badRequest().body("Failed to add transaction to ledger: " + e.problem);
        }
        return ResponseEntity.ok("{\"success\": true}");
    }

    private record TransactionDryRunStatus(boolean success, StockOrderException.Problem reason) {
    };

    @GetMapping("/holdings")
    public ResponseEntity<HoldingsResponse> getUserHoldings(@AuthenticationPrincipal StockUser user) {
        List<Holding> holdings = memeStockRepository.getHoldings(user.getUserId());
        return ResponseEntity.ok(new HoldingsResponse(holdings));
    }

    private record HoldingsResponse(List<Holding> holdings) {
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getUserBalance(@AuthenticationPrincipal StockUser user) {
        long balance = memeStockRepository.getAcctBalance(user.getUserId());
        return ResponseEntity.ok(new BalanceResponse(balance));
    }

    private record BalanceResponse(long balance) {
    }

    /**
     * Gets the financial history for the user. If possible, returns a single point
     * outside both sides of the range.
     */
    @GetMapping("accountHistory")
    public ResponseEntity<AccountHistoryResponse> getAccountHistory(
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @AuthenticationPrincipal StockUser user) {
        var start = OffsetDateTime.parse(startDate);
        var end = OffsetDateTime.parse(endDate);
        return ResponseEntity
                .ok(new AccountHistoryResponse(memeStockService.getAccountHistory(start, end, user.getUserId())));
    }

    private record AccountHistoryResponse(Map<OffsetDateTime, BalanceHoldingsPair> history) {
    };

    @PostMapping("/description")
    public ResponseEntity<UpdateBioResponse> setDescription(@RequestBody UpdateBioRequest request,
            @AuthenticationPrincipal StockUser user) {
        memeStockRepository.setUserBio(user.getUserId(), request.newBio());
        return ResponseEntity.ok(new UpdateBioResponse(request.newBio()));
    }

    private record UpdateBioResponse(String newBio) {
    };

    private record UpdateBioRequest(String newBio) {
    };

    private record CreateUserRequest(String email, String password) {
    };

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest req) {
        var result = userService.createAccount(req.email, req.password);

        if (result.isOk()) {
            return ResponseEntity.ok().body(result.unwrap());
        }

        switch (result.getErr()) {
            case AccountCreationError.EmailTaken:
                return ResponseEntity.unprocessableEntity().body("EMAIL_TAKEN");
            case AccountCreationError.Unknown:
            default:
                return ResponseEntity.internalServerError().build();
        }
    }

    private record VerifyUserRequest(String attemptId, String code) {}

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestBody VerifyUserRequest req) {
        var success = userService.verifyAccount(req.attemptId(), req.code);
        
        if (success) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.unprocessableEntity().body("WRONG_CODE");
    }
}
