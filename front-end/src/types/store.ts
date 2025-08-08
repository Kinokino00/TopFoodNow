export interface Store {
    id: number
    userId: number,
    storeId: number,
    storeName: string
    score: number
    photoUrl: string
}

export interface StoreDetails {
    id: number
    storeName: string
    address: string
    averageScore: number
    categoryNames: string[]
}