package main

import (

	//该包是用来使用框架接口的

	"encoding/json"
	"fmt"

	shim "github.com/tjfoc/tjfoc/core.v2/chaincode/shim" //该包是用来使用通信消息结构的
	pb "github.com/tjfoc/tjfoc/protos/chaincode"
)

// 合约方法处理器
type handler func(stub shim.ChaincodeStubInterface, args []string) pb.Response

//该结构是自定义结构，表示当前这份合约，该结构必须实现两个方法Init和Invoke
type MyChaincode struct {
	handlerMap map[string]handler
}

func newChaincodes() *MyChaincode {
	cc := &MyChaincode{}
	cc.handlerMap = map[string]handler{
		"analysiswhitelist": cc.analysiswhitelist,
	}
	return cc
}

// SalesInfo  销售数据.
type SalesInfo struct {
	CompanyID   string //公司编号
	CompanyName string //公司名
	Sales       int    //销售额
	Profit      int    //利润
	CustomerNum int    //客户量
	UnitPrice   int    //客户单价
	SaleDay     string //销售日期 格式: yyyy-MM-dd
	TimeStamp   int64  //写入销售数据的时间戳
	Pubkey      string // Saas方公钥base64格式
}

//
type WhiteList struct {
	CompanyID   string
	CompanyName string
	IsWhiteList bool
	Times       int
}

func main() {
	mycc := newChaincodes()
	err := shim.Start(mycc)
	if err != nil {
		fmt.Printf("Error starting my chaincode : %s", err)
	}
}
func (cc *MyChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success(nil)
}

func (cc *MyChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	if fn, has := cc.handlerMap[function]; has {
		return fn(stub, args)
	}
	return shim.Error(fmt.Sprintf("Not found action %s", function))
}

func (cc *MyChaincode) analysiswhitelist(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Invalid parameter")
	}
	// 验签
	// if !cc.verifySign([]byte(args[0]), []byte(args[1])) {
	// 	return shim.Error("Invalid signature")
	// }
	var sales SalesInfo
	var whiteList WhiteList
	err := json.Unmarshal([]byte(args[0]), &sales)
	if err != nil {
		return shim.Error(err.Error())
	}

	keyWhiteList := "comp_" + sales.CompanyID //根据公司名查询

	data, err := stub.GetState(keyWhiteList) //查询公司是否已存

	if data == nil {
		whiteList.CompanyID = sales.CompanyID
		whiteList.IsWhiteList = false
		whiteList.Times = 0
	}
	if data != nil {
		err = json.Unmarshal(data, &whiteList)
		if err != nil {
			return shim.Error(err.Error())
		}
	}

	if sales.Sales >= 10000 { //查询是否符合条件

		whiteList.IsWhiteList = true
		whiteList.Times = 0
	} else {
		if whiteList.Times > 5 { //连续不符合超过5次则变false
			whiteList.IsWhiteList = false
		}
		whiteList.Times++

	}
	value, err := json.Marshal(whiteList)
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState(keyWhiteList, value)
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success([]byte("success"))
}
