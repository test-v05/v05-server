创房选项
客户端建议使用通城麻将
//0 局数 8/16
//1 玩法ID 263宁远麻将
//2 支付方式
//4 将玩法 1.258将,0乱将
//5 飘分 不飘:0/飘分:11,12,13  其中选择飘分3可选1分,2分,3分/飘分2可选1分,2分/ 
//6 底分 1/2/3
//7 比赛人数 2/3/4
//8 红中做王 0否, 1是
//9 金马翻倍 0否, 1是
//10 抓鸟: 2/4/6
//11 一鸟全中 1是
//12 杠翻倍 1是
//13 王可出 1是


//19 加倍：0否，1是
//20 加倍分
//21 加倍数

//28 托管时间
//29 托管1单局 2全局 3 3局


//34 低于多少分加多少分
//35 低于多少分阈值

----------------------------------------------------------------------------------------------------------------

额外消息:房间广播
飘分:
request code:236 [1.服务器->客户端 选择飘分, 2.客户端->服务端 玩家选择的飘分值]
request body: ComMsg.ComRes  参数列表为创房时给定的飘分参数(0不飘/定飘:1,2,3/飘分:11,12,13) 其中 13可选范围1分,2分,3分/12可选范围1分,2分   [服务端->客户端] 通知客户端开始飘分

request body: ComMsg.ComReq  参数列表传递选择具体飘分 0/1/2/3 (该参数一定不为空)   [客户端->服务端]     客户端选择飘分完毕


response code:237  玩家选择完毕
response body:ComMsg.ComRes 参数列表内参数0为userId,参数1为236请求ComMsg.ComReq内携带的参数列表  [用于表示当前客户端可见其他客户端的飘分状态]

庄家出牌:单个推送
response code:306 ComMsg.ComRes 没有参数, 该消息在所有客户端飘分选择完毕后,仅仅通知某客户端(庄家)可以出牌了
----------------------------------------------------------------------------------------------------------------

小结门子
仅保留如下(原名称->新名称)以下索引已经从0开始:
0.碰碰胡
2.七小对
3.清一色
16.平胡
17.硬胡
33.金马翻倍