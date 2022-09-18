# 创建身份池
## 注意：
### 1.身份验证供应商 -> 自定义 -> 开发人员提供商名称 （这个是demo中的  providerName  ）
![0](images/img.png)

### 2.身份池 ID 也是demo中用到的参数
![1](images/img_1.png)





# 创建事务
## 按步骤创建事务
demo中的策略均以事务  ”ZYJTEST001“  所生成策略

![3](images/img_3.png)
![4](images/img_4.png)
![5](images/img_5.png)
![6](images/img_6.png)
![7](images/img_7.png)
![8](images/img_8.png) 

## 策略内容如下:
```{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "iot:Connect"
            ],
            "Resource": [
                "arn:aws-cn:iot:*:accountId:client/ZYJTEST001"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "iot:Publish"
            ],
            "Resource": [
                "arn:aws-cn:iot:*:accountId:topic/$aws/things/ZYJTEST001/shadow/*",
                "arn:aws-cn:iot:*:accountId:topic/productKey/ZYJTEST001/*",
                "arn:aws-cn:iot:*:accountId:topic/$aws/rules/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "iot:Receive"
            ],
            "Resource": [
                "arn:aws-cn:iot:*:accountId:topic/$aws/things/ZYJTEST001/shadow/*",
                "arn:aws-cn:iot:*:accountId:topic/productKey/ZYJTEST001/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "iot:Subscribe"
            ],
            "Resource": [
                "arn:aws-cn:iot:*:accountId:topicfilter/$aws/things/ZYJTEST001/shadow/*",
                "arn:aws-cn:iot:*:accountId:topicfilter/productKey/ZYJTEST001/*"
            ]
        }
    ]
}
```

    
![2](images/img_2.png)




# demo运行结果



![9](images/img_9.png)

![img_10.png](images/img_10.png)

与demo中的一致
![img_11.png](images/img_11.png)

![img_12.png](images/img_12.png)