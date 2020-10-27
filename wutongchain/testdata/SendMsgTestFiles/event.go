package main

import (
	"encoding/hex"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

type Event struct {
	Topic       string      `json:topic,omitempty`
	SubTopic    string      `json:subTopic,omitempty`
	Date        string      `json:date,omitempty`        //事件发生时间
	LedgerID    string      `json:ledgerID,omitempty`    //触发事件的链
	BlockHeight uint64      `json:blockHeight,omitempty` //触发事件的区块
	TxList      []TxInfo    `json:txList,omitempty`      //交易信息
	Client      string      `json:client,omitempty`      //事件发起方——sdk ID
	EventMsg    interface{} `json:eventMsg,omitempty`    //事件消息
	// Dump     bool      //持久化
}
type TxInfo struct {
	TxHash    []byte //触发事件的交易
	TxHashHex string
	TxIndex   uint //触发事件的交易索引
}

func main() {
	RunWeb("9300")
}

func RunWeb(port string) {
	gin.SetMode(gin.ReleaseMode)
	router := gin.Default()
	router.POST("/event", HandlerEvent)
	fmt.Println(fmt.Sprintf("服务启动...端口为%v...", port))
	router.Run(":" + port)
}

func HandlerEvent(c *gin.Context) {
	var req Event
	err := c.BindJSON(&req)
	if err != nil {
		c.String(http.StatusBadRequest, fmt.Sprintf("err : %s", err))
		return
	}

	for k := range req.TxList {
		req.TxList[k].TxHashHex = hex.EncodeToString(req.TxList[k].TxHash)
	}

	tmp, _ := json.Marshal(req)
	fmt.Printf("%s\n", string(tmp))

	c.String(http.StatusOK, "SUCCESS")
}
