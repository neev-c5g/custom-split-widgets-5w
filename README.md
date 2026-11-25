# 분할화면 커스텀 1.0.0

순정 오른쪽 분할화면을 열면 선택한 외부 앱의 위젯을 표시합니다. 앱에서 기능을 켜고 설치된 위젯을 선택할 수 있습니다. 외부 앱 연동 방법은 [위젯 SDK 및 개발 안내](widget-sdk/README.md)를 참고하세요.

- [NEMESIS USB 설치 ZIP](custom-split-main-1.0.0-nemesis.zip)
- [NEMESIS 패키지 ZIP](custom-split-main-1.0.0-nemesis-package.zip)
- [파일 무결성 SHA-256](SHA256SUMS)

HMG-API 1.0.2 이상이 필요합니다. USB 설치 ZIP은 압축을 풀어 USB의 최상위에 복사합니다. 다른 설치 패키지와 `NEMESIS.data`를 덮어쓰지 않도록 각각 따로 설치하세요. 앱을 설치한 후 차량 앱 목록에서 **분할화면 커스텀**을 열어 설정하세요.

아이오닉 5의 Gen5W Android 4.4 및 기존 테마 환경에서 순정 손잡이, 패널 닫기와 주차 카메라 버튼 동작을 확인했습니다. 다른 차종과 후진 카메라 진입, 테마 미설치 환경 및 장시간 사용은 확인하지 못했습니다. NEMESIS USB 설치 경로는 패키지 구조만 확인했으며 차량에서 직접 설치하지 않았습니다.

`app`, `hook`, `sdk`, `src`, `examples`, `tests`는 해당 버전의 소스입니다. 빌드에는 Android SDK와 HMG-API 소스, 별도로 보관한 서명 키가 필요합니다.
