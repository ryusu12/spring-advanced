## 1. [문제 인식 및 정의]

과제 `Lv 4. API 로깅`를 수행하기위해 AdminUserInterceptor.java와 AdminAspect.java를 작성했습니다.

처음으로 Interceptor와 AOP를 구현하여 작성했던 만큼, 코드의 가독성이 떨어진다 생각하여 두 코드의 리팩토링을 진행했습니다.


## 2. [해결 방안]
### 2-1. [의사결정 과정]
먼저 기존의 코드의 개선점들을 살펴봤습니다.

1. 부족한 주석
   - (AdminAspect.java) 코드 전체에 주석이 존재하지 않습니다.
   - (AdminUserInterceptor.java) 클래스와 메서드에 대한 주석이 필요합니다.
2. 미흡한 선언 부분
   - (AdminAspect.java) objectMapper를 `final`로 지정하지 않았습니다.
3. pointcut의 잘못된 접근 제어자
   - (AdminAspect.java) 외부에서 사용하지 않음에도 `public`으로 설정되어 있습니다.
4. 부정확한 메서드와 변수 이름
    - (AdminAspect.java) `getLog()`, `createLog()`와 같이 실제 기능과 맞지 않는 이름입니다.
5. if문 사용 방식
   - (AdminAspect.java) 주로 처리되는 과정이 if문 안으로 들어가있는 상태입니다.

### 2-2. [해결 과정]

1. 부족한 주석
   - JavaDoc을 활용하여 클래스와 메서드의 역할을 작성합니다.
2. 미흡한 선언 부분
    - objectMapper를 `private final`로 지정합니다.
3. pointcut의 잘못된 접근 제어자
    - 다른 위치에서 사용하지 않을 것은 `private`으로 변경합니다.
4. 부정확한 메서드와 변수 이름
   - 축약형보다 자세히 작성합니다.
5. if문 사용 방식
   - `result != null`으로 변경하여 주된 내용을 if문 밖으로 꺼냅니다.

## 3. [해결 완료]
### 3-1. [회고]

이번 과정을 통해 가독성을 높이는데 집중했습니다.

들여쓰기를 최소화하기 위해 if 문의 구성을 변경하거나, 주석을 추가하여 최대한 다른 사람이 코드를 이해하기 쉽게 하고자 했습니다.

이 과정에서 JavaDoc로 주석을 작성하는 것은 처음이라, 어떤 내용이 들어가야하는지 고민하는 시간이 생각보다 많이 필요했습니다.

주요 메서드는 `/** ... */`로, 그외 부가적인 메서드는 `/* ... */`로 작성하여 구분했습니다.

늘어나는 주석으로 인해 오히려 가독성이 떨어지지 않을까 라는 생각이 들면서, 여러번의 시도를 거쳐 효과적인 주석 작성법을 익히는 것이 중요함을 깨달았습니다.



### 3-2. [전후 데이터 비교]

1. 부족한 주석
    - 두 코드 모두 클래스와 메서드에 대한 주석이 추가되었습니다.
2. 미흡한 선언 부분
   - objectMapper를 `private final`로 지정했습니다.
   - log를 logger로 명확히 이름을 변경했습니다.
    ```java
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    ObjectMapper objectMapper = new ObjectMapper();
    // >>>>>
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();
    ```
3. pointcut의 잘못된 접근 제어자
   - `commentPointcut()`와 `userPointcut()`의 접근 제어자를 `private`으로 변경했습니다.
    ```java
    public void commentPointcut() {}
    public void userPointcut() {}
    // >>>>>>
    private void commentPointcut() {}
    private void userPointcut() {}
    ```
4. 부정확한 메서드와 변수 이름
    - 메서드 `getLog()`은 `logAdminRequestAdvice()`으로, `createLog()`는 `logAdminApiRequest()`로 변경했습니다.
    - 변수 `reqBody`와 `resBody`는 `requestBody`와 `responseBody`로 변경했습니다.
    ```java
    public Object getLog(ProceedingJoinPoint joinPoint) throws Throwable {}
    private void createLog(Long userId, String url, String reqBody, String resBody) {}
    // >>>>>>
    public Object logAdminRequestAdvice(ProceedingJoinPoint joinPoint) throws Throwable {}
    private void logAdminApiRequest(Long userId, String url, String requestBody, String responseBody) {}
    ```
5. if문 사용 방식
    - 사전에 `result == null`의 경우를 처리하여, 주로 처리되는 내용을 if문 밖으로 배치했습니다.
    ```java
    private String getResponseBody(Object result) throws JsonProcessingException {
        String resBody = null;
        if (result != null) {
            Map<String, Object> responseMap = objectMapper.convertValue(result, Map.class);
            resBody = objectMapper.writeValueAsString(responseMap.get("body"));
        }
        return resBody;
    }
    //>>>>>>>>>>
    private String getResponseBody(Object result) throws JsonProcessingException {
        if (result == null) return null;

        Map<String, Object> responseMap = objectMapper.convertValue(result, Map.class);
        return objectMapper.writeValueAsString(responseMap.get("body"));
    }
    ```