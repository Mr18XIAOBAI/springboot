package com.revisit.springboot;

import com.revisit.springboot.entity.partyinfo.PartyInfo;
import com.revisit.springboot.service.partyinfo.PartyInfoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SpringBootApplicationTestsAdd {

    @Autowired
    PartyInfoService partyInfoService;

    @Test
//    public void contextLoads() {
//       addPartyInfo();
//    }

    private void addPartyInfo() {

        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setType(1);
        partyInfo.setName("关于我们");
        partyInfo.setContent("这里是平台文字描述");
        System.out.println(partyInfoService);
        partyInfoService.addPartyInfo(partyInfo);
        PartyInfo partyInfo1 = new PartyInfo();
        partyInfo.setType(2);
        partyInfo.setName("服务协议");
        partyInfo.setContent("欢迎您使用软件及服务");
        partyInfoService.addPartyInfo(partyInfo1);

    }

}
