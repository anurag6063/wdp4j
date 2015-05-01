The goal of the project is to create a easy-used platform for build web application. Everyone (not only software engineer) can build a web form based word tables and system will create the JSP and the java code to save/update the data. Any enterprise employee can do that any time through the web and post the web form to participants. SE can build automatic and useful applications using the forms and classes. Of course,the most interesting things are that any software supplier can create useful module based on this platform. and the modules can been installed easily through web interface.Are you interesting in this platform,if so,please mail to me.
OK,That's all,every dear guys,let's go.

WDP4J（Web Development Platform for Java）使用说明


WDP4J是一套简化的、快速开发Web应用程序的基础平台，他基于简化的J2EE架构，采用Apache Struts 1.2、Hibernate 3.0等技术开发而成。使用该平台将大大减少程序代码开发量，并且能够快速构建应用程序原型。

平台是一套web应用程序，包括：模板设计器、视图（查询）设计器、用户管理模块、组织机构管理模块、角色管理模块、操作菜单管理模块、授权管理模块、后台程序等基本单元。


图1 系统框架图


> 如何使用平台进行开发


1、发布一套空白的程序（基于Tomcat）；

2、以root身份登录系统，按照应用需要在模板设计器中修改系统基本表单（例如用户信息表单等），然后重新编译所有表单（一键完成）；

3、添加系统用户、角色和组织机构等，并且分配操作菜单；

4、添加应用模块的用户输入表单（系统将自动创建数据库表进行存储）；

5、设计应用模块的可用视图（查询）；

6、添加对表单及视图所能进行的功能操作按钮，并且编写功能Actions；

7、为应用模块设计菜单，并且分配菜单；

8、单元测试；

9、开发其他模块；

10、集成测试；

11、正式发布。

> 开发平台适用范围

1、企事业单位内具有一般开发能力的组织和个人，利用平台为机构内部进行应用系统开发；

2、中小独立企业软件开发商，利用平台为客户快速建立系统原型；

3、独立个人软件开发者，利用平台为客户实现最有性价比的应用系统；

4、个人研究者及研究组织，利用平台架构设计更有竞争性的软件平台及产品；

> 使用平台所带来的利益

1、快速开发，缩短产品开发周期，为赢得市场取得先机；

2、减轻企业研发投入，6人月的薪水就可以拥有一套成熟的开发平台；

3、设计合理、简约，使系统具备可扩展性和高稳定性；

4、使用简单，降低了对开发人员的要求，一般程序开发人员就可以开发出健壮的程序；

5、一旦拥有，终身免费升级；

> 产品主要规格

操作系统：windows/Linux

数据库：oracle 9i-11g/MySQL5

JDK：SUN JDK 1.5及以上/OpenJDK 6及以上

Application Server：Tomcat5/ Tomcat6

其他依赖框架：Struts 1.2/Hibernate 3.0

模板设计方式：HTML

可输入表单元素：Text/Radio/Checkbox/Textarea/Select

支持数据类型：字符型/数值型/整形/日期型/超长文本

是否支持自动校验：是（自动产生Javascript脚本）

套打模式：客户端使用Word模板打印（较精确）

导出到EXCEL：支持

是否可以动态修改视图参数：可以，可修改显示模板、SQL、显示的列及列宽度、标题、对其方式、显示顺序等

是否支持多级组织结构：支持（无层级限制）