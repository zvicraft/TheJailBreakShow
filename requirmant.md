1. Role Division
    - Build a system that distinguishes between "Prisoners" and "Guards"
    - Define spectator mode
    - Implement server-side challenge for guard selection

2. Round System
    - Implement system for when prisoner/guard count reaches 0:
        - Fade to black transition
        - Players spawn in designated locations
        - Display "Day 2", "Day 3" etc. messages center screen
    - Randomize prisoner respawn locations each round
    - Enable Free Day system on Day 6

3. Free Day System
    - Activates automatically on Day 6
    - Guards can manually trigger Free Day
    - Implement both UI and command controls
    - Reference documentation for Free Day mechanics implementation

4. Currency System (Gold)
    - Set initial GOLD amount for new players
    - Configure reward parameters:
        - Gold rewards for prisoner killing guard
        - Other parameters per documentation

5. HUD System
    - Implement according to Reference image

# Chack List

- [X] Role Division | Complete
- [X] Round System | Complete
- [ ] Free Day System | Todo
- [ ] Currency System (Gold) | Todo
- [ ] HUD System | Todo
