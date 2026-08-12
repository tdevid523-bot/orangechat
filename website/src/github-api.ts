const REPO = 'tdevid523-bot/orangechat'
const API_URL = `https://api.github.com/repos/${REPO}/releases/latest`

export interface ReleaseAsset {
  name: string
  size: number
  browser_download_url: string
  download_count: number
}

export interface Release {
  tag_name: string
  name: string
  published_at: string
  html_url: string
  body: string
  assets: ReleaseAsset[]
}

export async function fetchLatestRelease(): Promise<Release | null> {
  try {
    const res = await fetch(API_URL)
    if (!res.ok) return null
    return await res.json()
  } catch {
    return null
  }
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}