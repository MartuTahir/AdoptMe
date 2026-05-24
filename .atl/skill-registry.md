# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

See `_shared/skill-resolver.md` for the full resolution protocol.

## User Skills

| Trigger | Skill | Path |
| --- | --- | --- |
| Create Gentle AI pull requests with issue-first checks. Trigger: creating, opening, or preparing PRs for review. | branch-pr | /home/jeronimo-guemberena/.config/opencode/skills/branch-pr/SKILL.md |
| Trigger: PRs over 400 lines, stacked PRs, review slices. Split oversized changes into chained PRs that protect review focus. | chained-pr | /home/jeronimo-guemberena/.config/opencode/skills/chained-pr/SKILL.md |
| Design docs that reduce cognitive load. Trigger: writing guides, READMEs, RFCs, onboarding, architecture, or review-facing docs. | cognitive-doc-design | /home/jeronimo-guemberena/.config/opencode/skills/cognitive-doc-design/SKILL.md |
| Write warm, direct collaboration comments. Trigger: PR feedback, issue replies, reviews, Slack messages, or GitHub comments. | comment-writer | /home/jeronimo-guemberena/.config/opencode/skills/comment-writer/SKILL.md |
| Helps users discover and install agent skills when they ask questions like "how do I do X", "find a skill for X", "is there a skill that can...", or express interest in extending capabilities. This skill should be used when the user is looking for functionality that might exist as an installable skill. | find-skills | /home/jeronimo-guemberena/.agents/skills/find-skills/SKILL.md |
| Trigger: Go tests, go test coverage, Bubbletea teatest, golden files. Apply focused Go testing patterns. | go-testing | /home/jeronimo-guemberena/.config/opencode/skills/go-testing/SKILL.md |
| Create Gentle AI issues with issue-first checks. Trigger: creating GitHub issues, bug reports, or feature requests. | issue-creation | /home/jeronimo-guemberena/.config/opencode/skills/issue-creation/SKILL.md |
| Get best practices for developing applications with Spring Boot. | java-springboot | /home/jeronimo-guemberena/.agents/skills/java-springboot/SKILL.md |
| Trigger: judgment day, dual review, adversarial review, juzgar. Run blind dual review, fix confirmed issues, then re-judge. | judgment-day | /home/jeronimo-guemberena/.config/opencode/skills/judgment-day/SKILL.md |
| SDD phase-support skill for solving Programación III Backend Java exams using layered architecture, optimal algorithms, efficient data structures, and explicit technical justification. | programacion-backend-java-optimizer | /home/jeronimo-guemberena/.gemini/skills/programacion-backend-java-optimizer/SKILL.md |
| Trigger: new skills, agent instructions, documenting AI usage patterns. Create LLM-first skills with valid frontmatter. | skill-creator | /home/jeronimo-guemberena/.config/opencode/skills/skill-creator/SKILL.md |
| Plan commits as reviewable work units. Trigger: implementation, commit splitting, chained PRs, or keeping tests and docs with code. | work-unit-commits | /home/jeronimo-guemberena/.config/opencode/skills/work-unit-commits/SKILL.md |

## Compact Rules

Pre-digested rules per skill. Delegators copy matching blocks into sub-agent prompts as `## Project Standards (auto-resolved)`.

### branch-pr

- PRs require an approved issue (status:approved) and exactly one type:* label.
- Branch names must match ^(feat|fix|chore|docs|style|refactor|perf|test|build|ci|revert)\/[a-z0-9._-]+$.
- Use PR template, include linked issue (Closes/Fixes/Resolves #N).
- Run shellcheck on modified scripts before PR.
- Conventional commits only; no Co-Authored-By trailers.

### chained-pr

- Split PRs over 400 changed lines unless size:exception accepted.
- Keep each PR reviewable in ≤60 minutes and one deliverable unit.
- Each chained PR must include dependency diagram marking current PR.
- Choose one chain strategy and do not mix.
- Ensure diffs only include the current work unit.

### cognitive-doc-design

- Lead with outcome, use progressive disclosure and chunking.
- Prefer tables/checklists for recognition over recall.
- Provide quick path then details, keep sections focused.
- For PRs, call out review order and out-of-scope items.

### comment-writer

- Start with actionable point, keep 1–3 short paragraphs.
- Explain why for requested changes.
- Match thread language; use Rioplatense Spanish if Spanish.
- No em dashes.

### find-skills

- Use when user asks to find/install skills.
- Check skills.sh leaderboard before searching.
- Verify installs, source reputation, and stars before recommending.
- Offer install command and link to skills.sh.

### go-testing

- Prefer table-driven tests; test behavior not internals.
- Use t.TempDir for filesystem tests.
- Skip slow integrations with testing.Short().
- For Bubbletea: test Model.Update(), use teatest for interactive flows.
- Golden files must be deterministic; update via -update and rerun.

### issue-creation

- Use templates; blank issues are disabled.
- Issues get status:needs-review; maintainer adds status:approved before PR.
- Questions go to Discussions, not issues.
- Fill all required template fields and pre-flight checks.

### java-springboot

- Prefer constructor injection with private final fields.
- Use DTOs in controllers; validate with Bean Validation.
- Services are stateless; use @Transactional at correct granularity.
- Use @ControllerAdvice for consistent error handling.
- Use Testcontainers for integration tests when relevant.

### judgment-day

- Launch two blind judges in parallel; do not self-review.
- Ask before fixing Round 1 confirmed issues.
- Re-judge after fixes; terminal state only APPROVED or ESCALATED.
- Use skill registry standards in judge/fix prompts.

### programacion-backend-java-optimizer

- Use Java 21 features where appropriate; avoid overengineering.
- Choose algorithms and data structures before coding; justify Big-O.
- Prefer simple loops when clearer than streams.
- Optimize by reducing nested loops and duplicate traversals.

### skill-creator

- If docs/skill-style-guide.md exists, follow it first.
- Keep SKILL.md 180–450 tokens (max 1000) and include required frontmatter.
- Use Activation Contract, Hard Rules, Decision Gates, Execution Steps, Output Contract, References.
- Put examples/templates in assets/ and extra detail in references/.

### work-unit-commits

- Commit by deliverable work unit, not file type.
- Keep tests and docs with the behavior they verify.
- Each commit should stand alone and be rollback-friendly.
- If change approaches 400 lines, plan chained PR slices.

## Project Conventions

| File | Path | Notes |
| --- | --- | --- |
| (none) | — | No project-level convention files found. |
