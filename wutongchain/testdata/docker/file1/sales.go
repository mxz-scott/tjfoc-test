//该包是用来使用框架接口的

package main

import (
	//"encoding/base64"
	"encoding/json"
	"fmt"
	//"time"

	shim "github.com/tjfoc/tjfoc/core.v2/chaincode/shim" //该包是用来使用通信消息结构的
	pb "github.com/tjfoc/tjfoc/protos/chaincode"
)

// 合约方法处理器
type handler func(stub shim.ChaincodeStubInterface, args []string) pb.Response

// MyChaincode 该结构是自定义结构，表示当前这份合约，该结构必须实现两个方法Init和Invoke
type MyChaincode struct {
	handlerMap map[string]handler
}

func newChaincodes() *MyChaincode {
	cc := &MyChaincode{}
	cc.handlerMap = map[string]handler{
		"addSalesInfo": cc.addSalesInfo,
	}
	return cc
}

// SalesInfo  销售数据.
type SalesInfo struct {
	CompanyID   string //公司编号
	Sales       int    //销售额
	Profit      int    //利润
	CustomerNum int    //客户量
	UnitPrice   int    //客户单价
	SaleDay     string //销售日期 格式: yyyy-MM-dd
	TimeStamp   int64  //写入销售数据的时间戳
	Pubkey      string // Saas方公钥base64格式
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

// 写入每日销售数据
// 参数 args 必须为长度为3的字符数组
// args[0]： 销售数据的JSON格式
// args[1]: Saas合作方对销售数据的数字签名
// args[2]: 白名单合约的版本号
func (cc *MyChaincode) addSalesInfo(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 5{
		return shim.Error("Invalid parameter")
	}

	var sales SalesInfo
	err := json.Unmarshal([]byte(args[0]), &sales)
	if err != nil {
		return shim.Error(err.Error())
	}

	// 以公司ID 作为Key
	key := "comp_" + sales.CompanyID  //根据公司名
	existData, err := stub.GetState(key)
	if existData != nil {
		return shim.Error("this data is exist!")
	}
	err = stub.PutState(key, []byte(args[0]))
	if err != nil {
		return shim.Error(err.Error())
	}

	// 组装白名单合约调用参数
	var callArg [][]byte
	callArg = append(callArg, []byte("analysiswhitelist"))
	callArg = append(callArg, []byte(args[0]))
	return stub.InvokeChaincode(args[3], string(args[2]),args[4],callArg) //调用白名单合约
}

func (cc *MyChaincode) verifySign(msg, sign, pubkey []byte) bool {
	//TODO: 实现签名验证
	return true
}
