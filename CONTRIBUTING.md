# How we work

1. `main` is protected: no direct pushes, no force-pushes.
2. Branch from an up-to-date `main` for each change: `feature/<slice>-<topic>` or `docs/<topic>`.
3. Keep branches short-lived (merge within a few days) and commits atomic: one logical change per commit.
4. Commit messages: imperative summary line (max ~60 chars), then a body that explains **why**.
5. Open a pull request to `main`. The other teammate reviews it; at least one approving review is required.
6. `mvn clean package` must pass locally before requesting review.
7. Squash-free merge (merge commit) so each atomic commit stays visible in history.
8. AI-assisted commits carry a `Co-authored-by` trailer naming the tool; the human author owns the change.
