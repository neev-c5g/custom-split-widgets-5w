# 분할화면 커스텀 위젯 SDK 1.0.0

분할화면 커스텀 1.0.0에서 다른 앱의 위젯을 표시하기 위한 Android 소스 SDK입니다. 호스트와 위젯 앱은 별도 APK이며 프로토콜 버전은 **1**입니다. Android 4.4(API 19)에서 동작합니다.

[SDK 전체 ZIP 다운로드](custom-split-widget-sdk-1.0.0.zip) · [호스트 설치](../README.md) · [시계 예제](examples/clock)

## 시작하기

1. `source/kr/blackr/split/sdk/WidgetProtocol.java`를 위젯을 제공하는 앱의 소스에 포함합니다.
2. `examples/clock/AndroidManifest.xml`처럼 `exported` 서비스에 액션 `kr.blackr.customsplit.action.WIDGET_PROVIDER`와 `ID`, `TITLE`, `VERSION` 메타데이터를 선언합니다. ID는 영문 소문자로 시작하고 소문자·숫자·`_`·`.`·`-`만 쓰며 최대 80자, 제목은 최대 60자입니다. `VERSION`은 정수 `1`입니다. 서비스/패키지 이름을 포함한 식별자는 최대 240자입니다.
3. `examples/clock`처럼 `Messenger` 서비스를 구현합니다. 호스트가 보낸 `SUBSCRIBE`의 버전과 인스턴스 토큰을 확인하고 `replyTo`에 `FRAME`을 보냅니다. 프레임 `Bundle`에 동일한 `version`, `instance`, 앱 자신의 패키지로 생성한 `remote_views`를 넣습니다. 호스트는 첫 프레임을 3초 동안 기다립니다.
4. `UNSUBSCRIBE`, 바인딩 종료, 전송 실패 또는 서비스 종료 시 프레임 생성과 관련 리스너를 중지합니다. 앱 설치만으로는 표시되지 않습니다. 사용자가 분할화면 커스텀 앱에서 위젯을 활성화해야 합니다.

시계 예제는 구독자 하나를 처리합니다. 여러 동시 호스트를 지원해야 하는 앱은 인스턴스 토큰별 세션과 자원 수명을 직접 관리해야 합니다. 호스트는 화면이 닫히거나 다른 위젯을 선택하면 구독을 해제합니다.

## 크기와 화면

프로토콜 v1은 너비·높이·밀도 정보를 제공자에게 보내지 않습니다. 호스트가 `RemoteViews`를 현재 패널의 사용 가능 영역에 `match_parent`로 적용하므로 고정 픽셀 크기를 가정하지 말고 유연한 레이아웃과 `dp`/`sp`를 사용하세요. 순정 패널 크기와 여백은 기기 및 테마에 따라 달라질 수 있습니다. 지원되는 Android 4.4 `RemoteViews` 뷰와 메서드만 사용하고 실제 대상 차량에서 확인하세요.

## 버튼과 앱 열기

`RemoteViews.setOnClickPendingIntent`에 제공 앱의 명시적 `PendingIntent`를 사용합니다. 재생·정지 등은 제공 앱의 서비스 또는 리시버로 액션을 보내 **기존 앱의 동일한 상태와 제어 경로**를 사용하고, 상태 변경 시 새 프레임을 전송하세요. 호스트에 별도의 재생 제어 API는 없습니다. 액션마다 다른 `action` 또는 요청 코드를 사용하여 `PendingIntent`가 서로 덮어쓰이지 않게 합니다.

전체 화면 앱을 여는 버튼은 앱 Activity를 명시적으로 실행하고, 앱 측 백그라운드 스레드에서 다음을 호출하면 호스트에 패널 닫기를 요청할 수 있습니다.

```java
try {
    getContentResolver().call(
        android.net.Uri.parse("content://kr.blackr.customsplit.config"),
        "request_close", null, null);
} catch (RuntimeException ignored) {
    // 호스트가 없거나 요청이 거부될 수 있습니다.
}
```

패널 기능이 켜져 있고 호출 앱이 사용자가 활성화한 제공자여야 합니다. 요청은 비동기적으로 처리되며 닫힘을 동기적으로 보장하지 않습니다. 해당 API가 실패해도 앱 화면을 사용할 수 있도록 처리하세요. 닫힌 순정 손잡이는 차량이 담당하며 SDK가 임의로 순정 화면을 제어하지 않습니다.

## 보안과 검증 범위

호스트는 선택한 서비스에 명시적으로 바인딩하고 토큰·버전·`RemoteViews` 패키지를 검사합니다. Android 4.4의 제한 때문에 이 검사만으로 제공자에 대한 완전한 인증을 보장하지 않습니다. 제공자의 `PendingIntent`는 제공 앱의 권한으로 실행되므로 외부에서 서비스나 리시버를 호출할 수 있는지, 버튼 액션과 입력 검증을 각 앱에서 설계해야 합니다. 예제의 공개 서비스는 개념 설명용입니다.

택시미터기 제공자와 앱 열기 요청은 아이오닉 5 Gen5W 차량에서 확인했습니다. 다른 차종·테마, 다수 제공자, 장시간 사용 및 e-ASD 연동은 별도 검증이 필요합니다. SDK는 소스와 예제로 구성되며 별도의 Gradle 라이브러리나 Maven 패키지를 제공하지 않습니다.
