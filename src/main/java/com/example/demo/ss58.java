package com.example.demo;

import io.emeraldpay.polkaj.ss58.SS58;
import io.emeraldpay.polkaj.ss58.SS58Codec;
import io.emeraldpay.polkaj.ss58.SS58Type;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class ss58 {
    public static void main(String[] args) throws DecoderException {
        encode();
        decode();
    }

    public static void encode() throws DecoderException {
        byte[] pubkey = Hex.decodeHex(
                // a pubkey is 32 byte value, for this example it's hardcoded as hex
                "e5be9a5092b81bca64be81d212e7f2f9eba183bb7a90954f7b76361f6edb5c0a"
        );
        String address = SS58Codec.getInstance().encode(
                // using Kusama here. but for Polkadot mainnet use SS58Type.Network.LIVE
                SS58Type.Network.CANARY,
                // pubkey as bytes
                pubkey
        );
        System.out.println("Address: " + address);
    }

    public static void decode() {
        SS58 address = SS58Codec.getInstance().decode("HNZata7iMYWmk5RvZRTiAsSDhV8366zq2YGb3tLH5Upf74F");

        if (address.getType() != SS58Type.Network.CANARY) {
            throw new IllegalStateException("Not Kusama address");
        }

        System.out.println(
                "Pub key: " + Hex.encodeHexString(address.getValue())
        );
    }
}
