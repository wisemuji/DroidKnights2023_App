package com.droidknights.app.core.data.repository

import app.cash.turbine.test
import com.droidknights.app.core.data.api.fake.FakeGithubRawApi
import com.droidknights.app.core.data.datastore.fake.FakeSessionPreferencesDataSource
import com.droidknights.app.core.model.Level
import com.droidknights.app.core.model.Room
import com.droidknights.app.core.model.Session
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime

internal class DefaultSessionRepositoryTest : StringSpec() {

    init {
        val repository: SessionRepository = DefaultSessionRepository(
            githubRawApi = FakeGithubRawApi(),
            sessionDataSource = FakeSessionPreferencesDataSource()
        )
        "역직렬화 테스트" {
            val expected = Session(
                id = "1",
                title = "키노트",
                content = "",
                speakers = emptyList(),
                level = Level.ETC,
                tags = emptyList(),
                room = Room.ETC,
                startTime = LocalDateTime(2024, 6, 11, 10, 40),
                endTime = LocalDateTime(2024, 6, 11, 11, 0),
                isBookmarked = false
            )
            val actual = repository.getSessions()
            actual.first() shouldBe expected
        }

        "북마크 추가 테스트" {
            repository.getBookmarkedSessionIds().test {
                awaitItem() shouldBe emptySet()

                repository.bookmarkSession(sessionId = "1", bookmark = true)
                awaitItem() shouldBe setOf("1")

                repository.bookmarkSession(sessionId = "2", bookmark = true)
                awaitItem() shouldBe setOf("1", "2")
            }
        }

        "북마크 제거 테스트" {
            // given : [1, 2, 3]
            val bookmarkedSessionIds = listOf("1", "2", "3")
            bookmarkedSessionIds.forEach {
                repository.bookmarkSession(it, true)
            }

            repository.getBookmarkedSessionIds().test {
                awaitItem() shouldBe setOf("1", "2", "3")

                // [1, 2, 3] -> [1, 3]
                repository.bookmarkSession(sessionId = "2", bookmark = false)
                awaitItem() shouldBe setOf("1", "3")

                // [1, 3] -> [1]
                repository.bookmarkSession(sessionId = "3", bookmark = false)
                awaitItem() shouldBe setOf("1")
            }
        }
    }
}
