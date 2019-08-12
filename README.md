# ClassFinal

#### 介绍
java class文件加密工具


#### 加密

```
参数说明
-file        加密的jar/war完整路径
-packages    加密的包名(可为空,多个用","分割)
-libjars     jar/war包lib下要加密jar文件名(多个用","分割)
-exclude     排除的类名(可为空,多个用","分割)
-pwd         加密密码
-classes     加密的classes目录
-libs        项目依赖的jar包目录(多个用","分割)
-Y           无需确认
-C           加密class目录，加密war解压的那种项目就加这个参数
```


##### 示例1: 加密普通jar、springboot的jar或springweb的war

```
java -jar this.jar -file springboot.jar -libjars a.jar,b.jar -packages com.yourpackage,com.yourpackage2 -exclude com.yourpackage.Main -pwd 123456 -Y
```

结果: 生成 springboot-encrypted.jar，这个就是加密后的jar文件；加密后的文件不可直接执行，需要配置javaagent。

```
参数说明:
-file springboot.jar                          springboot.jar就是需要加密的包，可以是jar或war
-libjars a.jar,b.jar                          jar/war里边lib目录下的jar包，如果需要加密，就把文件名加上
-packages com.yourpackage,com.yourpackage2    过滤包名，只有是这些包下的类才被加密
-exclude com.yourpackage.Main                 排除不需要加密的类全名
-pwd 123456                                   密码
-Y                                            不加此参数会确认以上信息
```

##### 示例2: 加密已经解压过的war包

```
java -jar this.jar -classes /tomcat/webapps/your_project/WEB-INF/classes -libs /tomcat/lib,/tomcat/webapps/your_project/WEB-INF/lib -packages com.yourpackage,com.yourpackage2 -exclude com.yourpackage.Main -pwd 995800 -Y
```

结果: 在/tomcat/webapps/your_project/WEB-INF/classes下生成classes.dat文件；WEB-INF/classes下的类文件已经被加密，忘记密码将无法启动。

```
参数说明:
-classes /tomcat/webapps/your_project/WEB-INF/classes       要加密的WEB-INF/classes目录
-libs /tomcat/lib,/tomcat/webapps/your_project/WEB-INF/lib  整个项目依赖的jar包，不会被加密
-packages com.yourpackage,com.yourpackage2                  过滤包名，只有是这些包下的类才被加密
-exclude com.yourpackage.Main                               排除不需要加密的类全名
-pwd 995800                                                 密码
-Y                                                          不加此参数会确认以上信息
```

注:
以上两个示例是直接用参数执行，也可以直接执行 java -jar this.jar按照步骤提示输入信息完成加密。


====================
    启动加密后的jar
====================

加密后的项目需要设置javaagent来启动项目，项目在启动过程中解密class，完全内存解密，不留下任何解密后的文件。

-----------------------------
参数说明
-data        加密后的jar/war完整路径
-pwd         密码

-----------------------------
示例1: 启动普通jar、springboot的jar

执行: java -javaagent:this.jar='-data /project/springboot-encrypted.jar -pwd 0000000' -jar /project/springboot-encrypted.jar

参数说明:
-data /project/springboot-encrypted.jar 加密后生成的jar包
-pwd 0000000                            密码

-----------------------------
示例2: tomcat运行解压过的war包
tomcat catalina.sh 增加以下配置:
CATALINA_OPTS="$CATALINA_OPTS -javaagent:this.jar='-data /tomcat/webapps/your_project/WEB-INF/classes/classes.dat,/tomcat/webapps/your_project2/WEB-INF/classes/classes.dat -pwd 0000000,123123'";
export CATALINA_OPTS;


参数说明:
-data classes.dat,classes2.dat  加密后生成的dat文件，一个tomcat可能会有多个项目，所以多个dat文件用逗号隔开
-pwd 0000000,123123             多个dat依次对应的密码

注:多个项目相同包名类型可能会冲突


