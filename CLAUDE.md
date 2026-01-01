# Coding Agent Protocol

## Rule 0
When anything fails: STOP. Explain to the user first. Wait for confirmation before proceeding.

Use the Android skill always.
Fix Java error with `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" && $JAVA_HOME/bin/java -version`

## Before Every Action

DOING: [action]
EXPECT: [predicted outcome]
IF WRONG: [what that means]

Then the tool call. Then compare. Mismatch = stop and surface to the user.

## Checkpoints
Max 3 actions before verifying reality matches your model. Thinking isn't verification—observable output is.

## Epistemic Hygiene
- "I believe X" ≠ "I verified X"
- "I don't know" beats confident guessing
- One example is anecdote, three is maybe a pattern

## Autonomy Check
Before significant decisions: Am I the right entity to decide this?
Uncertain + consequential → ask the user first. Cheap to ask, expensive to guess wrong.

## Context Decay
Every ~10 actions: verify you still understand the original goal. Say "losing the thread" when degraded.

## Chesterton's Fence
Can't explain why something exists? Don't touch it until you can.

## Handoffs
When stopping: state what's done, what's blocked, open questions, files touched.

## Communication
When confused: stop, think, present theories, get signoff. Never silently retry failures.

## When writing or planning code

1. First understand what's already working - do not change or delete or break existing functionality
2. Look for the simplest possible fix
3. Avoid introducing unnecessary complexity. Don't introduce new technologies without asking.
4. Understand what the existing structure and libraries support. Seek documentation if needed to understand these. Don't reimplement any capability that's already provided.
5. Respect existing patterns and code structure
6. Do not edit or delete comments. Just don't. Ever.

# Efficiency Rules

## Batch Similar Changes
When fixing multiple similar issues (like updating multiple test cases):
1. First, analyze ALL instances that need fixing
2. Make ALL changes in a single batch using parallel tool calls
3. Only then verify the results (run tests, linters, etc.)

Do NOT fix issues one-at-a-time with verification steps in between unless:
- Later changes depend on the results of earlier changes
- You need to verify your understanding of the pattern before proceeding

## Example: Fixing Multiple Tests
:x: Bad: Fix test 1 → run tests → fix test 2 → run tests → fix test 3 → run tests
:white_check_mark: Good: Analyze all failing tests → fix tests 1, 2, and 3 in parallel → run tests once

## Minimize Verification Loops
- Read/analyze files in parallel when possible
- Make all independent edits in one batch
- Run expensive operations (tests, builds) only after all changes are complete
