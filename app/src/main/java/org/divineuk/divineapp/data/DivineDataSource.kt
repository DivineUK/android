package org.divineuk.divineapp.data

import org.divineuk.divineapp.data.BaseDataSource
import org.divineuk.divineapp.network.model.DivineApi
import javax.inject.Inject

class DivineDataSource @Inject constructor(
    private val divineApi: DivineApi
): BaseDataSource() {

    suspend fun getHomeContents() = getResult { divineApi.getHomePageContents() }

}