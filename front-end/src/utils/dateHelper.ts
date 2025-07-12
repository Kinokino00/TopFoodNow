import moment from 'moment'
import 'moment/locale/zh-tw'

/**
 * 日期類別
 */
export enum DateTypeEnum {
  Daily = 1,
  Weekly = 2,
  Monthly = 3,
  SingleQuarter = 4,
  FullYearQuarters = 5,
  EachYear = 6,
}

export enum DayType {
  Monday = 0,
  Tuesday = 1,
  Wednesday = 2,
  Thursday = 3,
  Friday = 4,
  Saturday = 5,
  Sunday = 6,
}

/**
 * 取得當週的日期
 * @param date
 * @returns
 */
export const getWeekMap = (date: Date): Map<DayType, Date> => {
  const weekMap = new Map<DayType, Date>()

  if (date) {
    const weekOfDay: number = +moment(date).format('E')
    const startDay: Date = moment(date)
      .subtract(weekOfDay - 1, 'days')
      .toDate()

    for (let i = 0; i < 7; i++) {
      const day: Date = moment(startDay).add(i, 'days').toDate()
      weekMap.set(i, day)
    }
  }
  return weekMap
}

/**
 * 取得當週的起訖日期(文字)
 * @param date
 * @returns
 */
export const getWeekStartAndEndDisplay = (date: Date): string => {
  if (date) {
    const weekOfDay: number = Number(moment(date).format('E'))
    const weekCount: number = Number(moment(date).format('ww'))
    const thisMonday = moment(date)
      .subtract(weekOfDay - 1, 'days')
      .format('YYYY-MM-DD')
    const thisSunday = moment(date)
      .add(7 - weekOfDay, 'days')
      .format('MM-DD')

    return `${thisMonday} ~ ${thisSunday} (W${weekCount})`
  }
  return ''
}

export const getSingleSeasonDisplay = (date: Date): string => {
  if (date) {
    const year = moment(date).format('YYYY')
    const season = moment(date).format('Q')
    return `${year}年第${season}季`
  }
  return ''
}

/**
 * 取得當週的起訖日期(日期)
 * @param date
 * @returns [0]:星期一 [1]:星期日
 */
export const getWeekStartAndEndDate = (date: Date): Date[] => {
  if (date) {
    const weekOfDay: number = +moment(date).format('E')
    const thisMonday = moment(date)
      .subtract(weekOfDay - 1, 'days')
      .toDate()
    const thisSunday = moment(date)
      .add(7 - weekOfDay, 'days')
      .toDate()

    return [thisMonday, thisSunday]
  }
  return []
}

/**
 * 取得日期字串 (參考: https://momentjs.com/docs/#/parsing/string-format/)
 * @param date
 * @param formatString
 * @returns
 */
export const getDateFormat = (date: Date, formatString: string): string => {
  if (date) {
    return moment(date).format(formatString)
  }
  return ''
}

/**
 * 根據時間類型取得正確的時間格式
 * @param dateType
 * @param date
 * @returns 2023-11-29 / 2023-W48 / 2023-11 / 2023-Q4 / 2023
 */
export const getDateTypeDateStr = (dateType: string, date: Date): string => {
  let format = ''
  switch (dateType) {
    case 'Daily':
      format = DateStringFormateMap.get(DateTypeEnum.Daily) as string
      break
    case 'Weekly':
      format = DateStringFormateMap.get(DateTypeEnum.Weekly) as string
      break
    case 'Monthly':
      format = DateStringFormateMap.get(DateTypeEnum.Monthly) as string
      break
    case 'SingleQuarter':
      format = DateStringFormateMap.get(DateTypeEnum.SingleQuarter) as string
      break
    case 'FullYearQuarters':
      format = DateStringFormateMap.get(DateTypeEnum.EachYear) as string
      break
    case 'EachYear':
      format = DateStringFormateMap.get(DateTypeEnum.EachYear) as string
      break
  }
  return getDateFormat(date, format)
}

/**
 * 取得日期字串格式
 */
export const DateStringFormateMap = new Map<DateTypeEnum, string>([
  [DateTypeEnum.Daily, 'YYYY-MM-DD'],
  [DateTypeEnum.Weekly, 'YYYY-[W]ww'],
  [DateTypeEnum.Monthly, 'YYYY-MM'],
  [DateTypeEnum.SingleQuarter, 'YYYY-[Q]Q'],
  [DateTypeEnum.FullYearQuarters, 'YYYY'],
  [DateTypeEnum.EachYear, 'YYYY'],
])
