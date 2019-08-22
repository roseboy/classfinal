# ClassFinal

## 介绍
ClassFinal是一款java class文件安全加密工具，支持直接加密jar包或war包，无需修改任何项目代码，兼容spring-framework；可避免源码泄漏或字节码被反编译。

##### Gitee: https://gitee.com/roseboy/classfinal

## 项目模块说明
* **classfinal-core:** ClassFinalde的核心模块，几乎所有加密的代码都在这里；
* **classfinal-fatjar:** ClassFinal打包成独立运行的jar包；
* **classfinal-maven-plugin:** ClassFinal加密的maven插件；

## 功能特性
* 无需修改原项目代码，只要把编译好的jar/war包用本工具加密即可。
* 支持普通jar包、springboot jar包以及普通java web项目编译的war包。
* 支持spring framework、swagger等需要在启动过程中扫描注解或生成字节码的框架。
* 支持maven插件，添加插件后在打包过程中自动加密。
* 支持加密WEB-INF/lib或BOOT-INF/lib下的依赖jar包。

## 环境依赖
JDK 1.8 +

## 使用说明
### 加密

执行以下命令
```
java -jar classfinal-fatjar.jar -file yourpaoject.jar -libjars a.jar,b.jar -packages com.yourpackage,com.yourpackage2 -exclude com.yourpackage.Main -pwd 123456 -Y
```

```
参数说明
-file        加密的jar/war完整路径
-packages    加密的包名(可为空,多个用","分割)
-libjars     jar/war包lib下要加密jar文件名(多个用","分割)
-exclude     排除的类名(可为空,多个用","分割)
-pwd         加密密码
-Y           无需确认，不加参数会提示确认以上信息
```

结果: 生成 yourpaoject-encrypted.jar，这个就是加密后的jar文件；加密后的文件不可直接执行，需要配置javaagent。

> 注:
> 以上示例是直接用参数执行，也可以直接执行 java -jar classfinal-fatjar.jar按照步骤提示输入信息完成加密。

### maven插件方式

在要加密的项目pom.xml中加入以下插件配置,目前最新版本是：1.0.5。
```xml
<plugin>
    <groupId>net.roseboy</groupId>
    <artifactId>classfinal-maven-plugin</artifactId>
    <version>${classfinal.version}</version>
    <configuration>
        <password>000000</password>
        <packages>com.yiyon,net.roseboy,yiyon</packages>
        <excludes>org.spring</excludes>
        <libjars>
            yiyon-reimburse-1.0.0.jar,yiyon-labor-1.0.0.jar,yiyon-income-1.0.0.jar,yiyon-contract-1.0.0.jar,yiyon-budget-1.0.0.jar,yiyon-bonus-1.0.0.jar,yiyon-basedata-1.0.0.jar,jeee-core-1.0.0.jar,jeee-admin-1.0.0.jar,jeee-apimgr-1.0.0.jar,jeee-code-1.0.0.jar,jeee-filereview-1.0.0.jar,jeee-importer-1.0.0.jar,jeee-workflow-1.0.0.jar
        </libjars>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>classFinal</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
运行mvn package时会在target下自动加密生成yourpaoject-encrypted.jar。

maven插件的参数名称与直接运行的参数相同，请参考上节的参数说明。

### 启动加密后的jar

加密后的项目需要设置javaagent来启动项目，项目在启动过程中解密class，完全内存解密，不留下任何解密后的文件。

解密功能已经自动加入到 yourpaoject-encrypted.jar中，所以启动时-javaagent与-jar相同，不需要额外的jar包。
启动jar项目执行以下命令：

```
java -javaagent:yourpaoject-encrypted.jar='-pwd 0000000' -jar yourpaoject-encrypted.jar
```


```
参数说明
-pwd         密码
```



### tomcat下运行加密后的war

tomcat catalina 增加以下配置:
```
//linux下 catalina.sh
CATALINA_OPTS="$CATALINA_OPTS -javaagent:classfinal-fatjar.jar='-pwd 0000000'";
export CATALINA_OPTS;

//win下catalina.bat
set CATALINA_OPTS="%CATALINA_OPTS% -javaagent:classfinal-fatjar.jar='-pwd 0000000'"

```

```
参数说明:
-pwd 0000000             密码
```

> 本工具使用AES算法加密class文件，密码是保证不被破解的关键，请保存好密码，请勿泄漏。
> 密码一旦忘记，项目不可启动且无法恢复，请牢记密码。

