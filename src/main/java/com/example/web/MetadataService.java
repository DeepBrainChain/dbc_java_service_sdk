package com.example.web;

import io.emeraldpay.polkaj.api.PolkadotApi;
import io.emeraldpay.polkaj.api.StandardCommands;
import io.emeraldpay.polkaj.apihttp.PolkadotHttpApi;
import io.emeraldpay.polkaj.scale.ScaleExtract;
import io.emeraldpay.polkaj.scaletypes.Metadata;
import io.emeraldpay.polkaj.scaletypes.MetadataReader;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Repository
public class MetadataService {

    private PolkadotApi api;

    @PostConstruct
    public void init() {
        api = PolkadotHttpApi.newBuilder().build();
    }

    public Metadata get() {
        try {
            return api.execute(StandardCommands.getInstance().stateMetadata())
                    .thenApply(ScaleExtract.fromBytesData(new MetadataReader()))
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //it's just an example app, so it's ok to skip all proper verifications
        return null;
    }

}
