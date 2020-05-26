# common-api
## 特性：

1.基于单表的增删改，减少不必要的重复劳动 

2.借见mongo查询数据，来完成API的查询

3.依赖于springboot2.x 

4.防SQL注入

5.黑名单控制，禁止查询某张表，某个字段

## 快速开始：
```pom
<dependency>
    <groupId>com.github.alenfive</groupId>
    <artifactId>common-api-boot-starter</artifactId>
    <version>1.0.1.RELEASE</version>
</dependency>
```

一旦引入`common-api-boot-starter`,将自动创建以下接口：    

1.POST /common/{table}/{resultType} 单表查询接口
 
2.POST /common/{table}  新增

3.PUT /common/{table}  修改

4.DELETE /common/{table}  删除

开始访问：
``` shell
curl -i -X POST \
   -H "Content-Type:application/json" \
   -d \
'{
  "filter":{
    "id":"1"
  }
}' \
 'http://localhost:8080/common/user/first'

#result:
{
    "id":1,
    "name":"hello"
}
 ```

##### 详细语法
##### 1.POST /common/{table}/{resultType}
table: 	对应数据库表名,如果表名为`user_info`,这里为`/common/query/userInfo/{resultType}`,自动将下划线转为驼峰，如果表为有统一前缀，如`t_user_info`,可以在yml配置中配置`common-api.table-prefix=t_`,来忽略前缀

resultType: 	它有三种值（`first` | `list` | `page`）,分别对应返回的三种格式。first：返回格式为`{}`,list返回格式为`[{}]`,page返回格式为：`{"pageSize":15,"pageNo":1,"data":[]}`

入参：		

| 字段  | 类型 | 描述  |
| ------------ | ------------ |------------ |
|  fields | arrays | ["id","head_img headImg"].指定返回的字段，默认返回所有字段  |
|  pageSize |  Integer | 当resultType=(list,page)时有效，默认15  |
|  pageNo |  Integer |  当resultType=page时有效，默认1 |
|  sort | LinkedHashMap  |  排序,eg:{"id":"desc","name":"asc"} |
|  filter |  Map |  查询条件 |

`filter` 解释:

```
{
          "account":"test",  
          "start_time":{	 
                 "$gte":"2020-03-13 00:00:00",
      	         "$gt":"2020-03-13 00:00:00",
      	         "$lte":"2020-03-20 00:00:00",
      	         "$lt":"2020-03-20 00:00:00"
           },
      	   "type":{"$ne":2},
      	   "name":{"$like":"%hello%"},
      	   "$and":{
      		 "status":3,
      		 "account":"001"
             },
      	   "$or":{
      		 "status":3,
      		 "account":"001"
            },
      	    "configure":["1111",1001]
}
```

| 查询关键字  | 描述  |
| ------------ | ------------ |
|  :  | 等值匹配 =  |
|  :[]  | 查询 in |
|  $gte |  大于等于 >= |
|  $lte |  小于等于 <= |
|  $gt |  大于 > |
|  $lt |  小于 < |
|  $ne |  不等于 != |
|  $and |  and 嵌套(key1=value1 and key2=value2) |
|  $or |  or 嵌套(key1=value1 or key2=value2) |

生成的查询为:
`where account = 'test' and start_time >= '2020-03-13 00:00:00' and start_time > '2020-03-13 00:00:00' and start_time <= '2020-03-20 00:00:00' and start_time < '2020-03-20 00:00:00' and type != 2 and name like '%hello%' and (status = 3 and account = '001') and (status = 3 or account = '001') and configure in ('1111',1001)`

##### 2.POST /common/{table}  新增
body:
```json
{
  "name":"test",
  "sex":1
}
```
##### 3.PUT /common/{table}  修改
body:

```json
{
	"filter":{
		"id":1
	},
	"updateSet":{
		"name":"hello1"
	}
}
```
##### 4.DELETE /common/{table}  删除
body:

```json
{
	"filter":{
		"id":1
	}
}
```



#### 配置:

| 属性  | 描述 |
| ------------ | ------------ |
|  common-api.table-prefix | 表名统一前缀,eg: "t\_"  。默认 "" |
|  common-api.black-table-field-list | 访问黑名单控制,eg: t_content: \      - title  ,map结构，key为表名，value为字符，value="*" 表示禁用该表所有字段访问|

示例：
```
common-api:
  table-prefix: t_
  black-table-field-list:
    t_user:
      - password
    t_user_info:
      - '*'
```

#### 更多例子

```java

1/根据ID查询，返回id,name字段
POST /common/query/user/first
{
	"fields":["id","name"],
	"filter":{
		"id":"1111"
	}
}
2/in查询
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"id":["1",2]
	}
}
3/like查询
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"name":{"$like":"%富贵%"}
	}
}
4/范围查询 大于，小于
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"start_time":{
			"$gte":"2020-03-13 00:00:00",
			"$lte":"2020-03-20 00:00:00"
		}
	}
}
5/反向查询,不等于
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"type":{"$ne":1}
	}
}
6/嵌套逻辑范围查询，$and,$or
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"group_account":"001",
		"$or":{
			"type":{"$ne":1},
			"name":{"$like":"富贵%"}
		}
	}
}
where group_account='001' and (type !=1 or name like '富贵%')

7/根据关键字模糊匹配,倒序前5条记录
POST /common/query/user/list
{
	"fields":["id","name"],
	"filter":{
		"name":{"$like":"%宝贵%"}
	},
	"sort":{"time":"desc"},
	"pageSize":5
}

8/分页查询
POST /common/query/user/page
{
	"fields":["id","name"],
	"filter":{
		"name":{"$like":"%宝贵%"}
	},
	"sort":{"time":"desc"},
	"pageSize":5,
	"pageNo":1
}
```
