export interface RecommendItem {
    id: number
    userId: number
    storeId: number
    storeName: string
    storeAddress: string
    reason: string
    score: number
    categoryNames: string[]
    createdAt: string
    photoUrl?: string
}

export interface Pageable {
    pageNumber: number
    pageSize: number
    sortBy: string
    sortOrder: string
}

export interface ApiResponse<T> {
    data: T[]
    pageable: Pageable
    totalElements: number
    totalPages: number
}