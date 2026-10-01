SHELL := /bin/sh

.PHONY: help agents-list skills-list agents-check

help:
	@printf '%s\n' \
	  'Yeyecatl repository helpers' \
	  '' \
	  '  make agents-list   List configured agents' \
	  '  make skills-list   List configured skills' \
	  '  make agents-check  Validate agent/skill files'

agents-list:
	@find .github/agents -maxdepth 1 -type f -name '*.agent.md' -print | sort

skills-list:
	@find .github/skills -mindepth 2 -maxdepth 2 -type f -name 'SKILL.md' -print | sort

agents-check:
	@python3 -c "from pathlib import Path; import sys; files=list(Path('.github/agents').glob('*.agent.md'))+list(Path('.github/skills').glob('*/SKILL.md')); bad=[str(p) for p in files if not p.read_text(encoding='utf-8').startswith('---\\n')]; print(f'validated {len(files)} agent/skill files'); print('invalid frontmatter: '+', '.join(bad)) if bad else None; sys.exit(bool(bad))"
