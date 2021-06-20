package com.example.demo;


import io.emeraldpay.polkaj.api.*;
import io.emeraldpay.polkaj.apiws.PolkadotWsApi;
import io.emeraldpay.polkaj.json.RuntimeVersionJson;
import io.emeraldpay.polkaj.scale.ScaleExtract;
import io.emeraldpay.polkaj.scaletypes.AccountInfo;
import io.emeraldpay.polkaj.scaletypes.Metadata;
import io.emeraldpay.polkaj.scaletypes.MetadataReader;
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel;
import io.emeraldpay.polkaj.ss58.SS58Type;
import io.emeraldpay.polkaj.tx.AccountRequests;
import io.emeraldpay.polkaj.tx.ExtrinsicContext;
import io.emeraldpay.polkaj.types.*;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;


public class transfer {


    private static final DotAmountFormatter AMOUNT_FORMAT = DotAmountFormatter.autoFormatter();


    public static void main(String[] args) throws Exception {
        String api = "wss://info.dbcwallet.io";
        if (args.length >= 1) {
            api = args[0];
        }
        System.out.println("Connect to: " + api);

        Schnorrkel.KeyPair aliceKey;
        Address alice;
        Address bob;
        if (args.length >= 3) {
            System.out.println("Use provided addresses");
            aliceKey = Schnorrkel.getInstance().generateKeyPairFromSeed(Hex.decodeHex(args[1]));
            bob =  Address.from(args[2]);
        } else {
            System.out.println("Use standard accounts for Alice and Bob, expected to run against development network");
            aliceKey = Schnorrkel.getInstance().generateKeyPairFromSeed(
                    Hex.decodeHex("e44534d42e13e5fbce16fd1073aba2e63744303eb188f50aa7d2342353dcf8b6")
            );
            bob =  Address.from("5D9koUJqsKYP7xscNZeok4zFWinQqceR1mccnxwSGzi6APPo");
        }
        alice = new Address(SS58Type.Network.CANARY, aliceKey.getPublicKey());

        Random random = new Random();
        DotAmount amount = DotAmount.fromDots(
                100
        );
//        DotAmount amount = DotAmount.fromPlancks(
//                Math.abs(random.nextLong()) % DotAmount.fromDots(0.002).getValue().longValue() //随机产生
//        );

//        DotAmount amount = DotAmount.fromDots(
//                100
//        );

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

            // Subscribe to balance updates
            AccountRequests.AddressBalance aliceAccountRequest = AccountRequests.balanceOf(alice);
            AccountRequests.AddressBalance bobAccountRequest = AccountRequests.balanceOf(bob);
            client.subscribe(
                    StandardSubscriptions.getInstance()
                            .storage(Arrays.asList(
                                    // need to provide actual encoded requests
                                    aliceAccountRequest.encodeRequest(),
                                    bobAccountRequest.encodeRequest())
                            )
            ).get().handler((event) -> {
                event.getResult().getChanges().forEach((change) -> {
                    AccountInfo value = null;
                    Address target = null;
                    if (aliceAccountRequest.isKeyEqualTo(change.getKey())) {
                        value = aliceAccountRequest.apply(change.getData());
                        target = alice;
                    } else if (bobAccountRequest.isKeyEqualTo(change.getKey())) {
                        value = bobAccountRequest.apply(change.getData());
                        target = bob;
                    } else {
                        System.err.println("Invalid key: " + change.getKey());
                    }
                    if (value != null) {
                        System.out.println("Balance update. User: " + target + ", new balance: " + AMOUNT_FORMAT.format(value.getData().getFree()));
                    }
                });
            });

            // get current runtime metadata to correctly build the extrinsic
            Metadata metadata = client.execute(
                    StandardCommands.getInstance().stateMetadata()
            )
                    .thenApply(ScaleExtract.fromBytesData(new MetadataReader()))
                    .get();

            // prepare context for execution
            ExtrinsicContext context = ExtrinsicContext.newAutoBuilder(alice, client)
                    .get()
                    .build();

            // get current balance to show, optional
            AccountInfo aliceAccount = aliceAccountRequest.execute(client).get();

            System.out.println("Using genesis : " + context.getGenesis());
            System.out.println("Using runtime : " + context.getTxVersion() + ", " + context.getRuntimeVersion());
            System.out.println("Using nonce   : " + context.getNonce());
//            System.out.println("Using nonce   : " + context.getEra());
//            System.out.println("Using nonce   : " + context.getEraBlockHash());
            System.out.println("------");
  //          System.out.println("Currently available: " + AMOUNT_FORMAT.format(aliceAccount.getData().getFree()));
            System.out.println("Transfer           : " + AMOUNT_FORMAT.format(amount) + " from " + alice + " to " + bob);

            // prepare call, and sign with sender Secret Key within the context
            AccountRequests.Transfer transfer = AccountRequests.transfer()
                    .runtime(metadata)
                    .from(alice)
                    .to(bob)
                    .amount(amount)
                    .sign(aliceKey, context)
                    .build();

            ByteData req = transfer.encodeRequest();
            System.out.println("RPC Request Payload: " + req);
            Hash256 txid = client.execute(
                    StandardCommands.getInstance().authorSubmitExtrinsic(req)
            ).get();
            System.out.println("Tx Hash: " + txid);

            // wait for a few blocks, to show how subscription to storage changes works, which will
            // notify about relevant updates during those blocks
            waitForBlocks.get();
        }
    }
}
