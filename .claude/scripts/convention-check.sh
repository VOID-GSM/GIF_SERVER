#!/usr/bin/env bash
# 기계적으로 확실히 판별되는 컨벤션 위반만 검사한다. 판단이 필요한 규칙은 code-reviewer 에이전트가 본다.
# 사용법: convention-check.sh [BASE] [HEAD]
#   BASE 기본값 origin/develop. HEAD 생략 시 작업 트리(커밋 안 된 변경, 새 파일 포함)를 검사한다.
#   추가된 줄과 새 파일만 보므로 기존 코드의 위반은 보고하지 않는다.
# 출력: path:line: [규칙] 설명   (위반 없으면 exit 0, 있으면 exit 1)
set -u
git() { command git -c core.safecrlf=false "$@"; }
cd "$(git rev-parse --show-toplevel)" || exit 2
base=${1:-origin/develop}
head=${2:-}

fork=$(git merge-base "$base" "${head:-HEAD}") || { echo "merge-base 실패: '$base' 또는 '${head:-HEAD}' ref를 확인하세요" >&2; exit 2; }

# CLAUDE.md의 "<label>" 줄에서 백틱으로 감싼 단어를 읽어 A|B|C 형태로 돌려준다. 없으면 기본값을 쓰고 경고한다.
claude_md_list() {
  local label=$1 default=$2 list
  list=$(grep -m1 -- "$label" CLAUDE.md 2>/dev/null | grep -o '`[A-Za-z]*`' | tr -d '`' | paste -sd'|')
  if [ -z "$list" ]; then
    echo "CLAUDE.md에서 '$label' 줄을 찾지 못해 기본값($default)을 씁니다" >&2
    list=$default
  fi
  printf '%s' "$list"
}
dto_prefixes=$(claude_md_list '허용 접두어:' 'Get|Create|Patch|Delete')
entity_suffixes=$(claude_md_list '허용 Entity 접미어:' 'JpaEntity')
repository_suffixes=$(claude_md_list '허용 Repository 접미어:' 'JpaRepository')

if [ -n "$head" ]; then
  range=("$fork" "$head")
  show() { git show "$head:$1"; }
  untracked() { :; }
else
  range=("$fork")
  show() { cat "$1"; }
  untracked() { git ls-files --others --exclude-standard -- '*.java'; }
fi

added_lines() {
  git diff -U0 --no-color --diff-filter=AM "${range[@]}" -- '*.java' | awk '
    /^\+\+\+ b\// { file = substr($0, 7); next }
    /^@@/ { split($3, a, ","); line = substr(a[1], 2) + 0; next }
    /^\+/ { print file "\t" line "\t" substr($0, 2); line++ }
  '
  untracked | while read -r f; do awk -v f="$f" '{ print f "\t" NR "\t" $0 }' "$f"; done
}

new_files() {
  git diff --name-only --diff-filter=A "${range[@]}" -- '*.java'
  untracked
}

violations=$(
  added_lines | tr -d '\r' | awk -F'\t' '
    $1 ~ /\/service\// && $3 ~ /^import jakarta\.servlet\./ {
      print $1 ":" $2 ": [계층] service에서 HTTP 객체 사용 금지 (" $3 ")"
    }
    $1 ~ /\/controller\// && $3 ~ /^import .*\.repository\./ {
      print $1 ":" $2 ": [계층] controller가 repository를 직접 사용함 (service를 거쳐야 함)"
    }
    $3 ~ /@Table\(.*name *= *"/ {
      match($3, /name *= *"[^"]*"/); n = substr($3, RSTART, RLENGTH); sub(/^name *= *"/, "", n); sub(/"$/, "", n)
      if (n !~ /^[a-z0-9_]+$/) print $1 ":" $2 ": [테이블] 테이블명 \"" n "\"이 스네이크 표기법이 아님"
    }
  '
  new_files | sort -u | while read -r f; do
    name=$(basename "$f" .java)
    content=$(show "$f" | tr -d '\r')
    entity_line=$(grep -n '^@Entity' <<<"$content" | head -1 | cut -d: -f1)
    if [ -n "$entity_line" ]; then
      grep -q '@Table' <<<"$content" || echo "$f:$entity_line: [테이블] @Entity에 @Table(name = \"snake_case\") 누락"
      [[ $name =~ ($entity_suffixes)$ ]] || echo "$f:$entity_line: [네이밍] 새 Entity는 ${entity_suffixes//|//} 중 하나로 끝난다 (예: ${name%Entity}${entity_suffixes%%|*})"
    fi
    if [[ $f == */repository/* && $name == *Repository && ! $name =~ ($repository_suffixes)$ ]]; then
      echo "$f:1: [네이밍] 새 Repository는 ${repository_suffixes//|//} 중 하나로 끝난다 (예: ${name%Repository}${repository_suffixes%%|*})"
    fi
    if [[ $f == */dto/* && $name =~ (Request|Response)$ && ! $name =~ ^($dto_prefixes) ]]; then
      echo "$f:1: [네이밍] DTO는 ${dto_prefixes//|//} 중 하나로 시작한다 (현재: $name)"
    fi
  done
)

[ -z "$violations" ] && exit 0
printf '%s\n' "$violations"
exit 1
