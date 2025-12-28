package com.lbk.socialbanking;


import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTest {

    @Test
    public void applicationModules() {
        ApplicationModules modules = ApplicationModules.of(SocialBankingApplication.class);
        modules.forEach(System.out::println);
        modules.verify();

    }

    @Test
    public void writeDocumentation() {
        var modules = ApplicationModules.of(SocialBankingApplication.class).verify();
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
