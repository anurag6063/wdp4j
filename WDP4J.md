WDP4J is the meaning “Web Development Platform for JAVA”


Introduction

> 目前，各软件供应商分别使用各自的软件开发平台进行功能开发，普遍存在以下几个问题：一是不同开发商的软件很难集成到一起，二是各自维护开发平台，维护成本较高，WDJ4J的出现就是为了解决这个问题。通过建立一个开放的软件开发平台，使开发商不必开发自己的平台，同时可以更好地相互集成，从而使软件开发商更重视业务理解和设计。

Details

> WDP4J将使用JAVA为开发语言，将来可以扩展使用其他的语言，采用struts2的MVC结构，但是要对struts2进行精简，数据持久化使用hibernate，模块之间采取消息总线的结构，降低彼此之间的耦合性，该平台的数据处理过程是这样的：

> 浏览器发出一个请求指令（平台的所有指令统一管理）

> 2.struts2的filter进行处理，包括创建struts action和一个actionform，接收form提交的数据，然后将处理消息发送至消息中心（所有的对象和消息平台统一管理）

> 3.已经注册的事件监听器按照优先级对actionform进行处理，最后返回struts2

> 4.struts2将actionforward返送到客户端。