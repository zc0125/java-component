# Java工具包

# 方法封装和执行工具（execute-task）

## 背景
在java中对于方法封装，并作为对象类型传递，没有比较简单并好用的工具。

通过方法传递这种方式，可以将功能逻辑进行封装，并传递出去，执行处只需关心自身的功能逻辑即可，并在需要执行处直接执行，执行完成后仍可处理自身逻辑，适合策略的传递。

## 实现策略

通过函数接口，将方法不同参数方法转换成同一种对象，方便传递和调用。

可封装的方式分为两种：

- 只包含方法逻辑，不包含参数，择机传参调用。
- 包含方法逻辑和参数，将功能后置执行。

方法的封装范围包含无参方法、1-5参数方法（超过5个人参数可以使用对象进行封装）、任意参数方法，基本满足所有场景。

方法的参数类型和执行结果类型通过泛型约束自动转换，只需注重业务逻辑即可。

使用参考
```java
// 1.执行逻辑封装
TaskGenerate<String> taskGenerate = TaskGenerate.init(this::test1);
// 执行逻辑转成包含参数的任务
TaskExecutor<String> taskExecutor = taskGenerate.withArgs("test");
// 执行
String result = taskExecutor.execute();

// 2.直接封装任务
TaskExecutor<String> taskExecutor2 = TaskExecutor.init(this::test1, "test");
// 执行
String result2 = taskExecutor2.execute();
```

多任务异步执行

```java
// 多任务异步执行，并获取执行结果
List<TaskExecutor<String>> taskList = new ArrayList<>();
// 定义任务
taskList.add(TaskExecutor.init(obj::test1,"1"));
taskList.add(TaskExecutor.init(obj::test2,"1","2"));
taskList.add(TaskExecutor.init(obj::test3));
taskList.add(TaskExecutor.init(obj::test4));

// 异步执行批量任务
List<TaskResult<String>> taskResults = TaskExecutor.executeTasks(taskList);
// 获取任务结果
for (TaskResult<String> result : taskResults) {
    if(result.success){
        System.out.println(result.getResult());
    }else {
        System.out.println(result.exception.getMessage());
    }
}
```


# 方法绑定工具（binding-task）

## 背景

功能之间解耦，不同职责之间逻辑互不影响。

便于拓展，用于新策略或新功能0侵入接入。

## 实现策略

调用者不关心具体实现，只需知道调用什么功能，什么参数，即可获取到执行结果。

通过注解在方法和类上进行绑定`@TaskBinding`(作用类或方法)、`@TaskHandler`(作用方法，需配合TaskBinding作用于类使用)。

通过扫描注解，将策略（service、module、function）绑定一个或多个方法（同一策略的方法参数应相同），实现调用链路。


单个方法绑定：
```java
@TaskBinding(service = "test", module = "A")
public String test1(String a, Integer b) {
    return "test1";
}

/**
 * 单方法绑定使用
 */
public void run1() {
    String result = TaskBindingUtil.executeOne("test", "A", "", "a", 1);
}

```
多个方法绑定
```java
@TaskBinding(service = "test", module = "B")
public String test2(String a, String b) {
    return "test2";
}

@TaskBinding(service = "test", module = "B")
public String test22(String a, String b) {
    return "test22";
}
/**
 * 多方法绑定使用
 */
public void run2() {
    List<TaskBindingResult<String>> resultList = TaskBindingUtil.executeAll("test", "B", "", "a", "b");
}

```
handler用法
```java
@Service
@TaskBinding(service = "test", module = "strategy")
public class TestBindingStrategyService {

    @TaskHandler
    public String test(String a) {
        return "a";
    }

    @TaskHandler(function = "A")
    public String test1(String a, Integer b) {
        return "test1";
    }

    @TaskHandler(function = "B")
    public String test2(String a, String b) {
        return "test2";
    }

    @TaskHandler(function = "B")
    public String test22(String a, String b) {
        return "test22";
    }
}

/**
 * 单方法绑定使用
 */
public void run1() {
    String result = TaskBindingUtil.executeOne("test", "strategy", "A", "a", 1);
}

/**
 * 多方法绑定使用
 */
public void run2() {
    List<TaskBindingResult<String>> resultList = TaskBindingUtil.executeAll("test", "strategy", "B", "a", "b");
}
```