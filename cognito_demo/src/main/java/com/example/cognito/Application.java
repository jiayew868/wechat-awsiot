package com.example.cognito;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.AttachPolicyResult;
import com.amazonaws.services.iot.model.CreatePolicyRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Application {
    /**
     * 运行  demo  或 查看demo前，需要配合文档查看
     * 最好先查看项目中的  README.md
     *
     *
     */

    public static void main(String[] args) {

        //aws API 密钥
        String accessKey = "ak"; //for demo
        String secretKey = "sk"; //for demo
        //区域
        String region = Regions.CN_NORTH_1.getName();
        //身份池ID
        String identityPoolId = "cn-north-1:231bb17a-50bd-42e2-801c-4b5980cf4629";
        //固定域名 无需修改
        String providerNameID = "cognito-identity.cn-north-1.amazonaws.com.cn";
        //开发者名称    身份验证提供商->自定义->开发人员提供商名称
        String providerName = "granwin";

        //用户身份  自定义  必须确保唯一  可以用 微信的openId
        String identifier = UUID.randomUUID().toString();


//初始化客户端------------------------------------------------------------------------------------------------------------

        //cognito客户端
        AmazonCognitoIdentity identityClient = AmazonCognitoIdentityClient.builder().withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).build();
        //iot 客户端
        AWSIot awsIot = AWSIotClient.builder().withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).build();

//1.获取用户身份----------------------------------------------------------------------------------------------------------
        //获取身份
        GetOpenIdTokenForDeveloperIdentityRequest request = new GetOpenIdTokenForDeveloperIdentityRequest();
        request.setIdentityPoolId(identityPoolId);//指定身份池
        HashMap<String,String> logins = new HashMap<>();
        logins.put(providerName,identifier);//指定身份来源 和用户身份
        request.setLogins(logins);
        request.setTokenDuration(86400L);//设置token有效期
        GetOpenIdTokenForDeveloperIdentityResult response = identityClient.getOpenIdTokenForDeveloperIdentity(request);

        //身份id  永久且唯一
        String identityId = response.getIdentityId();
        //身份token   有效期与TokenDuration参数设置的一致
        String token = response.getToken();

//2.为身份创建策略--------------------------------------------------------------------------------------------------------
        //策略名称-自定义必须唯一
        String policyName = identifier+"_"+"policy";
        //生成策略
        String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Action\":[\"iot:Connect\"],\"Resource\":[\"arn:aws:iot:*:*:client/${cognito-identity.amazonaws.com:sub}\"],\"Effect\":\"Allow\"},{\"Action\":[\"iot:Subscribe\"],\"Resource\":[\"arn:aws:iot:*:*:topicfilter/$aws/things/*/shadow/get/*\",\"arn:aws:iot:*:*:topicfilter/$aws/things/*/shadow/*\",\"arn:aws:iot:*:*:topicfilter/*/*/user/update\",\"arn:aws:iot:*:*:topicfilter/granwin/${iot:ClientId}/message\"],\"Effect\":\"Allow\"},{\"Action\":[\"iot:Receive\"],\"Resource\":[\"arn:aws:iot:*:*:topic/granwin/${iot:ClientId}/message\",\"arn:aws:iot:*:*:topic/*/*/ZYJTEST001/shadow/*\",\"arn:aws:iot:*:*:topic/*/ZYJTEST001/user/get\"],\"Effect\":\"Allow\"},{\"Action\":[\"iot:Publish\"],\"Resource\":[\"arn:aws:iot:*:*:topic/*/*/ZYJTEST001/shadow/*\",\"arn:aws:iot:*:*:topic/*/ZYJTEST001/user/get\"],\"Effect\":\"Allow\"}]}";
        //创建策略
        CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
        createPolicyRequest.setPolicyName(policyName);
        createPolicyRequest.setPolicyDocument(policy);
        awsIot.createPolicy(createPolicyRequest);
        //将此略附加到当前用户的身份中，使身份具有策略权限
        AttachPolicyRequest attachPolicyRequest = new AttachPolicyRequest();
        attachPolicyRequest.setPolicyName(policyName);
        attachPolicyRequest.setTarget(identityId);
        awsIot.attachPolicy(attachPolicyRequest);

//3.获取用户凭证----------------------------------------------------------------------------------------------------------
        //获取身份凭证
        GetCredentialsForIdentityRequest getCredentialsForIdentityRequest = new GetCredentialsForIdentityRequest();
        getCredentialsForIdentityRequest.setIdentityId(identityId);//获取的身份id
        HashMap<String,String> loginIdentity = new HashMap<>();
        loginIdentity.put(providerNameID,token);//指定身份来源 和用户身份令牌
        getCredentialsForIdentityRequest.setLogins(loginIdentity);
        GetCredentialsForIdentityResult credentialsForIdentity = identityClient.getCredentialsForIdentity(getCredentialsForIdentityRequest);

        Credentials credentials = credentialsForIdentity.getCredentials();
        //用户aws凭证
        String awsAccessKeyId = credentials.getAccessKeyId();
        String awsSecretAccessKey = credentials.getSecretKey();
        Date expiration = credentials.getExpiration();
        String sessionToken = credentials.getSessionToken();

        System.out.println("----------------凭证原文--------------");
        System.out.println(String.format("【%s】：%s","credentials.getAccessKeyId()",awsAccessKeyId));
        System.out.println(String.format("【%s】：%s","credentials.getSecretKey()",awsSecretAccessKey));
        System.out.println(String.format("【%s】：%s","credentials.getExpiration()",expiration));
        System.out.println(String.format("【%s】：%s","credentials.getSessionToken()",sessionToken));
        System.out.println("-----------------流程---------------");
        System.out.println("创建身份池");
        System.out.println("设置身份验证提供商->自定义->设置开发者  demo中设置的身份池开发者为【granwin】");
        System.out.println("【granin】开发者--> 通过自定义生成的用户唯一标识【"+identifier+"】");
        System.out.println("在开发者所在身份池【"+identityPoolId+"】 生成了用户身份【"+identityId+"】");
        System.out.println("并且为身份申请了临时权限token【"+token+"】");
        System.out.println("------------给用户身份附加策略权限-------------");
        System.out.println("将策略【"+policyName+"】附加到了用户身份【"+identityId+"】");

        System.out.println("利用【token】为用户申请了有aws服务权限的临时凭证");
        System.out.println("------------凭证信息和使用说明-------------");
        System.out.println("【credentials.getAccessKeyId()】：对应客户端参数【awsAccessKeyId】");
        System.out.println("【credentials.getSecretKey()】：对应客户端参数【awsSecretAccessKey】");
        System.out.println("【credentials.getExpiration()】：凭证过期时间");
        System.out.println("【credentials.getSessionToken()】：对应客户端参数【sessionToken】");
        System.out.println("------------其他参数在客户端用途-------------");
        System.out.println("【identityId】：对应客户端参数【clientID】 其实就是身份id");
        System.out.println("【iot code设置中平台节点】：对应客户端参数【clientEndpoint】 iotCode 连接host");
        System.out.println("【credentials.getExpiration()】：凭证过期时间");
        System.out.println("【credentials.getSessionToken()】：对应客户端参数【sessionToken】");
        
    }
}
