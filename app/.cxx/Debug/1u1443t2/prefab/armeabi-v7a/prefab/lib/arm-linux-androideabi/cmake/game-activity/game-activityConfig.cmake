if(NOT TARGET game-activity::game-activity)
add_library(game-activity::game-activity STATIC IMPORTED)
set_target_properties(game-activity::game-activity PROPERTIES
    IMPORTED_LOCATION "/Users/juangarrido/.gradle/caches/transforms-4/aec30794bfe8beec66ffad97653059db/transformed/games-activity-1.2.2/prefab/modules/game-activity/libs/android.armeabi-v7a/libgame-activity.a"
    INTERFACE_INCLUDE_DIRECTORIES "/Users/juangarrido/.gradle/caches/transforms-4/aec30794bfe8beec66ffad97653059db/transformed/games-activity-1.2.2/prefab/modules/game-activity/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

if(NOT TARGET game-activity::game-activity_static)
add_library(game-activity::game-activity_static STATIC IMPORTED)
set_target_properties(game-activity::game-activity_static PROPERTIES
    IMPORTED_LOCATION "/Users/juangarrido/.gradle/caches/transforms-4/aec30794bfe8beec66ffad97653059db/transformed/games-activity-1.2.2/prefab/modules/game-activity_static/libs/android.armeabi-v7a/libgame-activity_static.a"
    INTERFACE_INCLUDE_DIRECTORIES "/Users/juangarrido/.gradle/caches/transforms-4/aec30794bfe8beec66ffad97653059db/transformed/games-activity-1.2.2/prefab/modules/game-activity_static/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

