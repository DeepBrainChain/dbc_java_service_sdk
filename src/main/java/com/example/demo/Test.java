package com.example.demo;

//import com.onehilltech.promises.Promise;
import com.onehilltech.promises.Promise;
import io.reactivex.Observable;
import io.reactivex.functions.Function3;
import org.polkadot.api.SubmittableExtrinsic;
import org.polkadot.api.promise.ApiPromise;
import org.polkadot.api.rx.ApiRx;
import org.polkadot.common.keyring.Keyring;
import org.polkadot.common.keyring.Types;
import org.polkadot.example.TestingPairs;
import org.polkadot.rpc.provider.ws.WsProvider;
import org.polkadot.types.rpc.ExtrinsicStatus;
import org.polkadot.types.type.Event;
import org.polkadot.types.type.EventRecord;
import org.polkadot.utils.UtilsCrypto;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Test {


    static String ALICE = "5GrwvaEF5zXb26Fz9rcQpDWS57CtERHpNehXCPcNoHGKutQY";
    static int AMOUNT = 10000;

    //static String endPoint = "wss://poc3-rpc.polkadot.io/";
    //static String endPoint = "wss://substrate-rpc.parity.io/";
    //static String endPoint = "ws://45.76.157.229:9944/";
    static String endPoint = "wss://innertest.dbcwallet.io";

    static void initEndPoint(String[] args) {
        if (args != null && args.length >= 1) {
            endPoint = args[0];
            System.out.println(" connect to endpoint [" + endPoint + "]");
        } else {
            System.out.println(" connect to default endpoint [" + endPoint + "]");
        }
    }

    static {
        //System.loadLibrary("jni");
        System.out.println("load ");
    }

    //-Djava.library.path=./libs
    public static void main(String[] args) throws InterruptedException {
        initEndPoint(args);

        WsProvider wsProvider = new WsProvider(endPoint);

        String BOB = "5FHneW46xGXgs5mUiveU4sbTyGBzmstUspZC92UhjJM694ty";

        Observable<ApiRx> apiRxObservable = ApiRx.create(wsProvider);

        apiRxObservable.flatMap((apiRx) -> {
            Types.KeyringOptions options = new Types.KeyringOptions(org.polkadot.utils.crypto.Types.KeypairType_SR);
            Keyring keyring = new Keyring(options);
            Types.KeyringPair alice = keyring.addFromUri("//Alice", null, options.getType());
            org.polkadot.api.Types.SubmittableExtrinsicFunction function = apiRx.tx().section("balances").function("transfer");
            SubmittableExtrinsic<Observable> transfer = function.call(BOB, 111);
            return transfer.signAndSend(alice, new org.polkadot.types.Types.SignatureOptions());
        }).subscribe((result) -> {
            System.out.println("rx result " + result);
        });

    }
}
