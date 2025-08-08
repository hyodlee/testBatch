# 빌드 가이드

이 프로젝트는 환경별 설정을 Maven 프로필로 관리합니다. 아래 명령을 사용하여 원하는 환경으로 빌드할 수 있습니다.

```bash
mvn -Plocal package   # 로컬 환경 빌드
mvn -Pdev package     # 개발 환경 빌드
mvn -Pprod package    # 운영 환경 빌드
```

각 프로필은 해당 환경의 `globals-<프로필>.properties` 파일을 사용하여 `globals.properties`를 생성합니다.
