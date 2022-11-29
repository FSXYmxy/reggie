package com.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

/**
 * @create: 2022/11/20 10:04
 */
public class SMSUtils {

    /**
     * 发送短信
     * @param phoneNumbers 接收短信的手机号码
     * @param signName 短信签名名称
     * @param templateCode 短信模板CODE
     * @param templateParam 短信模板变量对应的实际值
     */
    public static void sendMessage(String phoneNumbers, String signName, String templateCode, String templateParam){
        
        DefaultProfile profile = DefaultProfile.getProfile("cn-hunan", "LTAI5tJt42uzNitCN4e5vSdn", "N7DIsVHF2OusdmHJ6f7apoYm9Adh5u");
        /** use STS Token
         DefaultProfile profile = DefaultProfile.getProfile(
         "<your-region-id>",           // The region ID
         "<your-access-key-id>",       // The AccessKey ID of the RAM account
         "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
         "<your-sts-token>");          // STS Token
         **/
        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phoneNumbers);//接收短信的手机号码
        request.setSignName(signName);//短信签名名称
        request.setTemplateCode(templateCode);//短信模板CODE
        request.setTemplateParam(templateParam);//短信模板变量对应的实际值

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }
}
