#!/usr/bin/env bash
# Stop: Java 변경 상태가 마지막으로 확인한 상태와 다르면 컴파일하고, 실패하면 오류를 Claude에게 돌려준다(exit 2).
# 어떤 도구로 수정했든(Edit, Write, Bash) 작업 트리의 실제 diff로 판단한다.
cd "$CLAUDE_PROJECT_DIR" || exit 0
git() { command git -c core.safecrlf=false "$@"; }
ok_file=.claude/.compile-ok
attempts_file=.claude/.compile-attempts
MAX_ATTEMPTS=3

changes=$({
  git diff HEAD -- '*.java'
  git ls-files --others --exclude-standard -- '*.java' | while read -r f; do echo "+++ $f"; cat "$f"; done
} 2>/dev/null)
[ -z "$changes" ] && { rm -f "$attempts_file"; exit 0; }

hash=$(printf '%s' "$changes" | md5sum | cut -d' ' -f1)
[ "$hash" = "$(cat "$ok_file" 2>/dev/null)" ] && { rm -f "$attempts_file"; exit 0; }

output=$(./gradlew compileJava compileTestJava -q --console=plain 2>&1)
if [ $? -eq 0 ]; then
  echo "$hash" > "$ok_file"
  rm -f "$attempts_file"
  exit 0
fi

count=$(( $(cat "$attempts_file" 2>/dev/null || echo 0) + 1 ))
if [ "$count" -ge "$MAX_ATTEMPTS" ]; then
  echo "$hash" > "$ok_file"
  rm -f "$attempts_file"
  echo "{\"systemMessage\": \"컴파일 실패가 ${MAX_ATTEMPTS}번 반복되어 자동 수정을 멈췄습니다. ./gradlew compileJava 로 직접 확인하세요.\"}"
  exit 0
fi
echo "$count" > "$attempts_file"

{
  echo "컴파일 실패 (시도 ${count}/${MAX_ATTEMPTS}). 아래 오류를 고친 뒤 작업을 마치세요."
  printf '%s\n' "$output" | grep -E 'error:|\.java:[0-9]+' | sed 's/^[[:space:]]*//' | awk '!seen[$0]++' | head -40
} >&2
exit 2
