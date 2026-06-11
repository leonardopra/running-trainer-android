package com.leopra.runningtrainer.domain.model

val preRunRoutine: List<StretchExercise> = listOf(
    StretchExercise(
        id = "pre_leg_swings",
        name = "Leg Swings",
        description = "Stand beside a wall for balance. Swing one leg forward and backward in a controlled arc, gradually increasing range of motion. Keep your upper body upright and core engaged.",
        muscleGroups = listOf("Hip Flexors", "Hamstrings", "Glutes"),
        durationSeconds = 30,
        reps = "10 swings per side",
        isPreRun = true,
        youtubeQuery = "leg swings dynamic warm up running"
    ),
    StretchExercise(
        id = "pre_hip_circles",
        name = "Hip Circles",
        description = "Stand with feet shoulder-width apart, hands on hips. Make large slow circles with your hips, first clockwise then counterclockwise. Great for lubricating the hip joint before impact.",
        muscleGroups = listOf("Hip Flexors", "Glutes", "Lower Back"),
        durationSeconds = 30,
        reps = "10 circles each direction",
        isPreRun = true,
        youtubeQuery = "hip circles warm up dynamic stretch running"
    ),
    StretchExercise(
        id = "pre_high_knees",
        name = "High Knees",
        description = "Jog in place lifting your knees as high as possible with each step. Pump your arms in sync with your legs. This activates quads, hip flexors, and gets your heart rate up gradually.",
        muscleGroups = listOf("Quadriceps", "Hip Flexors", "Calves"),
        durationSeconds = 45,
        reps = "45 seconds",
        isPreRun = true,
        youtubeQuery = "high knees warm up running exercise"
    ),
    StretchExercise(
        id = "pre_butt_kicks",
        name = "Butt Kicks",
        description = "Jog in place kicking your heels up toward your glutes with each step. Keep your thighs perpendicular to the ground and knees pointing down. Great for activating hamstrings.",
        muscleGroups = listOf("Hamstrings", "Quadriceps"),
        durationSeconds = 45,
        reps = "45 seconds",
        isPreRun = true,
        youtubeQuery = "butt kicks warm up running exercise"
    ),
    StretchExercise(
        id = "pre_ankle_circles",
        name = "Ankle Circles",
        description = "Stand on one foot (use a wall for balance if needed). Lift the other foot slightly and rotate the ankle in slow, controlled circles. Finish both directions before switching feet.",
        muscleGroups = listOf("Ankles", "Calves", "Achilles Tendon"),
        durationSeconds = 20,
        reps = "10 circles each direction, each ankle",
        isPreRun = true,
        youtubeQuery = "ankle circles warm up before running"
    ),
    StretchExercise(
        id = "pre_walking_lunges",
        name = "Walking Lunges",
        description = "Take a large step forward into a lunge position, lowering your back knee toward the ground. Push off your front foot and bring the back leg forward into the next lunge. Keep torso upright.",
        muscleGroups = listOf("Quadriceps", "Glutes", "Hip Flexors", "Hamstrings"),
        durationSeconds = 40,
        reps = "10 lunges each leg",
        isPreRun = true,
        youtubeQuery = "walking lunges warm up running"
    )
)

val postRunRoutine: List<StretchExercise> = listOf(
    StretchExercise(
        id = "post_quad_stretch",
        name = "Standing Quad Stretch",
        description = "Stand on one foot, bend the other knee and hold your ankle behind you. Pull gently toward your glutes until you feel a stretch in the front of your thigh. Keep knees together and stand tall.",
        muscleGroups = listOf("Quadriceps"),
        durationSeconds = 45,
        reps = null,
        isPreRun = false,
        youtubeQuery = "standing quad stretch after running cool down"
    ),
    StretchExercise(
        id = "post_hamstring_stretch",
        name = "Standing Hamstring Stretch",
        description = "Stand with feet together, extend one leg forward with heel on the ground and toes up. Hinge forward from the hips (not waist) keeping your back flat until you feel the stretch behind your thigh.",
        muscleGroups = listOf("Hamstrings", "Lower Back"),
        durationSeconds = 45,
        reps = null,
        isPreRun = false,
        youtubeQuery = "standing hamstring stretch runners cool down"
    ),
    StretchExercise(
        id = "post_calf_stretch",
        name = "Wall Calf Stretch",
        description = "Stand facing a wall, place both hands on it. Step one foot back, keeping the heel flat on the ground and the leg straight. Lean forward until you feel a deep stretch in your calf and achilles.",
        muscleGroups = listOf("Calves", "Achilles Tendon"),
        durationSeconds = 45,
        reps = null,
        isPreRun = false,
        youtubeQuery = "wall calf stretch runners cool down"
    ),
    StretchExercise(
        id = "post_hip_flexor",
        name = "Hip Flexor Lunge Stretch",
        description = "Step into a deep lunge with one foot forward. Lower your back knee to the ground. Gently push your hips forward and down. You should feel the stretch at the front of your back hip.",
        muscleGroups = listOf("Hip Flexors", "Glutes", "Quadriceps"),
        durationSeconds = 45,
        reps = null,
        isPreRun = false,
        youtubeQuery = "hip flexor lunge stretch runners cool down"
    ),
    StretchExercise(
        id = "post_it_band",
        name = "IT Band Cross-Leg Stretch",
        description = "Stand and cross one leg behind the other. Lean sideways toward the front leg's side, reaching the opposite arm overhead. You will feel the stretch along the outer thigh and hip.",
        muscleGroups = listOf("IT Band", "Outer Thigh", "Glutes"),
        durationSeconds = 30,
        reps = null,
        isPreRun = false,
        youtubeQuery = "IT band standing stretch runners"
    ),
    StretchExercise(
        id = "post_piriformis",
        name = "Figure-Four Stretch",
        description = "Lie on your back, cross one ankle over the opposite knee forming a \"4\" shape. Grasp behind the uncrossed thigh and gently pull both legs toward your chest. Hold when you feel the deep glute stretch.",
        muscleGroups = listOf("Piriformis", "Glutes", "Hip Rotators"),
        durationSeconds = 45,
        reps = null,
        isPreRun = false,
        youtubeQuery = "figure four piriformis stretch runners floor"
    ),
    StretchExercise(
        id = "post_child_pose",
        name = "Child's Pose",
        description = "Kneel on the floor, sit back on your heels, then extend both arms forward on the ground. Lower your forehead to the floor. Breathe deeply and let gravity open the hips and lower back.",
        muscleGroups = listOf("Lower Back", "Hips", "Glutes", "Shoulders"),
        durationSeconds = 60,
        reps = "60 seconds",
        isPreRun = false,
        youtubeQuery = "child's pose stretch runners yoga cool down"
    )
)
