package com.example.demo;

import io.emeraldpay.polkaj.api.StandardSubscriptions;
import io.emeraldpay.polkaj.apihttp.PolkadotHttpApi;
import io.emeraldpay.polkaj.apiws.PolkadotWsApi;
import io.emeraldpay.polkaj.scaletypes.AccountInfo;
import io.emeraldpay.polkaj.tx.AccountRequests;
import io.emeraldpay.polkaj.types.Address;
import io.emeraldpay.polkaj.types.DotAmount;
import io.emeraldpay.polkaj.types.DotAmountFormatter;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class balance {

    public static void main(String[] args) throws Exception {
        String api = "wss://info.dbcwallet.io";
//        try (PolkadotHttpApi client = PolkadotHttpApi.newBuilder().build()) {
            DotAmountFormatter formatter = DotAmountFormatter.autoFormatter();
//
//            DotAmount total = AccountRequests.totalIssuance().execute(client).get();
//            System.out.println(
//                    "Total Issued: " +
//                            formatter.format(total)
//            );
        try (PolkadotWsApi client = PolkadotWsApi.newBuilder().connectTo(api).build()) {
            System.out.println("Connected: " + client.connect().get());

            // Subscribe to block heights
            AtomicLong height = new AtomicLong(0);
            CompletableFuture<Long> waitForBlocks = new CompletableFuture<>();
            client.subscribe(
                    StandardSubscriptions.getInstance().newHeads()
            ).get().handler((event) -> {
                long current = event.getResult().getNumber();
                System.out.println("Current height: " + current);
                if (height.get() == 0) {
                    height.set(current);
                } else {
                    long blocks = current - height.get();
                    if (blocks > 3) {
                        waitForBlocks.complete(current);
                    }
                }
            });

            Address address = Address.from("5GGu2iQBLXuys61zbSnzfBsVMYzwaM3FfMbpMAmsAiExSWcN");
            System.out.println("Address: " + address);

            AccountInfo balance = AccountRequests.balanceOf(address).execute(client).get();
            if (balance == null) {
                System.out.println("NO BALANCE");
                return;
            }

            StringBuilder status = new StringBuilder();
            status
                    .append("Balance: ")
                    .append(formatter.format(balance.getData().getFree()));

            if (!balance.getData().getFeeFrozen().equals(DotAmount.ZERO)
                    || !balance.getData().getMiscFrozen().equals(DotAmount.ZERO)) {
                status.append(" (frozen ")
                        .append(formatter.format(balance.getData().getFeeFrozen()))
                        .append(" for Fee, frozen ")
                        .append(formatter.format(balance.getData().getMiscFrozen()))
                        .append(" for Misc.)");
            }
            BigInteger dbc_amount=balance.getData().getFree().getValue().divide(new BigInteger("1000000000000000"));
            
            System.out.println(dbc_amount.longValue());
        }
    }
}
