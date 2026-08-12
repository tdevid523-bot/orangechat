import { useReveal } from './useReveal'

const QQ_GROUP = '1095981820'
const QQ_GROUP_LINK = `https://qm.qq.com/cgi-bin/qm/qr?from=app&p=android&jump_from=webapi&auth_key=&s_group_id=${QQ_GROUP}`

/* ─── Navbar ─── */
function Navbar() {
  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-cream-100/80 backdrop-blur-md border-b border-warm-200">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 h-16 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <img src="/icon.png" alt="" className="w-7 h-7 rounded" />
          <span className="heading-serif text-lg text-warm-900">橘瓣</span>
        </div>
        <a href={QQ_GROUP_LINK} target="_blank" rel="noopener noreferrer"
          className="text-xs tracking-[0.15em] uppercase text-warm-900 border border-warm-900 px-5 py-2 hover:bg-warm-900 hover:text-cream-100 transition-colors duration-300">
          QQ 群
        </a>
      </div>
    </nav>
  )
}

/* ─── Hero ─── */
function Hero() {
  return (
    <section className="min-h-screen flex items-center justify-center bg-cream-100">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 text-center">
        <p className="text-xs tracking-[0.3em] uppercase text-warm-400 mb-12 animate-fade-up">OrangeChat · 2026</p>
        <h1 className="heading-serif text-6xl md:text-8xl lg:text-9xl text-warm-900 mb-6 leading-[0.95] animate-fade-up" style={{ animationDelay: '0.05s' }}>
          不止是聊天
        </h1>
        <p className="heading-serif text-2xl md:text-3xl text-warm-400 mb-16 animate-fade-up" style={{ animationDelay: '0.1s' }}>
          更是<em className="text-accent-500 not-italic">生活在一起</em>的 AI 伴侣
        </p>
        <div className="flex gap-4 justify-center animate-fade-up" style={{ animationDelay: '0.15s' }}>
          <a href={QQ_GROUP_LINK} target="_blank" rel="noopener noreferrer"
            className="text-sm tracking-[0.1em] uppercase bg-warm-900 text-cream-100 px-8 py-3 hover:bg-warm-700 transition-colors duration-300">
            加入 QQ 群
          </a>
          <a href="https://github.com/tdevid523-bot/orangechat" target="_blank" rel="noopener noreferrer"
            className="text-sm tracking-[0.1em] uppercase border border-warm-300 text-warm-600 px-8 py-3 hover:border-warm-900 hover:text-warm-900 transition-colors duration-300">
            GitHub
          </a>
        </div>
      </div>
    </section>
  )
}

/* ─── Manifesto ─── */
function Manifesto() {
  const ref = useReveal()
  return (
    <section ref={ref} className="min-h-screen flex items-center justify-center bg-warm-900 text-cream-200">
      <div className="max-w-2xl mx-auto px-6 md:px-12 text-center reveal">
        <div className="w-12 h-px bg-accent-500 mx-auto mb-16"></div>
        <blockquote className="heading-serif text-2xl md:text-3xl lg:text-4xl leading-[1.6]">
          <p className="mb-6">AI 不应该只活在对话框里。</p>
          <p className="mb-6">它应该知道你在哪，</p>
          <p className="mb-6">知道你今天走了多少步，</p>
          <p className="mb-6">知道你昨晚几点睡的。</p>
          <p className="mb-10">它应该在你需要的时候，<br />主动出现。</p>
        </blockquote>
        <p className="text-accent-400 text-sm tracking-[0.2em] uppercase">橘瓣就是这样一个尝试</p>
        <div className="w-12 h-px bg-accent-500 mx-auto mt-16"></div>
      </div>
    </section>
  )
}

/* ─── Features ─── */
const features = [
  { num: '01', title: '插件系统', desc: 'QuickJS 沙箱框架，声明式 UI，5 个内置插件' },
  { num: '02', title: '生活感知', desc: '位置、附近搜索、App 统计、摄像头' },
  { num: '03', title: '健康数据', desc: 'Gadgetbridge 同步步数、心率、睡眠' },
  { num: '04', title: '记忆银行', desc: '完整生命周期管理，Supabase 云端同步' },
  { num: '05', title: '主动消息', desc: 'AI 不再被动等你，而是主动出现' },
  { num: '06', title: '个性定制', desc: '头像框、气泡、字体、思维链样式' },
]

function Features() {
  const ref = useReveal()
  return (
    <section id="features" ref={ref} className="min-h-screen flex items-center justify-center bg-cream-100 py-24">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 w-full">
        <div className="mb-20 reveal">
          <p className="text-xs tracking-[0.3em] uppercase text-warm-400 mb-4">Features</p>
          <h2 className="heading-serif text-5xl md:text-6xl text-warm-900">独有功能</h2>
        </div>
        <div className="grid md:grid-cols-2 gap-x-16 gap-y-12">
          {features.map((f, i) => (
            <div key={i} className="reveal group border-t border-warm-200 pt-6" style={{ transitionDelay: `${i * 50}ms` }}>
              <div className="flex items-baseline gap-4 mb-3">
                <span className="text-xs text-warm-300 font-mono">{f.num}</span>
                <h3 className="heading-serif text-2xl text-warm-900">{f.title}</h3>
              </div>
              <p className="text-warm-400 text-sm leading-relaxed pl-8">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ─── Tools ─── */
const tools = [
  'Alarm', 'AppUsage', 'Battery', 'Calendar', 'Camera',
  'Nearby', 'Health', 'Music', 'SMS', 'System', 'Zip'
]

function Tools() {
  const ref = useReveal()
  return (
    <section id="tools" ref={ref} className="min-h-screen flex items-center justify-center bg-cream-200 py-24">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 w-full">
        <div className="mb-20 reveal">
          <p className="text-xs tracking-[0.3em] uppercase text-warm-400 mb-4">Tools</p>
          <h2 className="heading-serif text-5xl md:text-6xl text-warm-900">15 个内置工具</h2>
        </div>
        <div className="reveal flex flex-wrap gap-3">
          {tools.map((t, i) => (
            <span key={i} className="border border-warm-300 text-warm-600 text-sm px-4 py-2 hover:bg-warm-900 hover:text-cream-100 hover:border-warm-900 transition-colors duration-300 cursor-default">
              {t}
            </span>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ─── Architecture ─── */
function Architecture() {
  const ref = useReveal()
  return (
    <section id="architecture" ref={ref} className="min-h-screen flex items-center justify-center bg-cream-100 py-24">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 w-full">
        <div className="mb-20 reveal">
          <p className="text-xs tracking-[0.3em] uppercase text-warm-400 mb-4">Architecture</p>
          <h2 className="heading-serif text-5xl md:text-6xl text-warm-900">核心架构</h2>
        </div>
        <div className="grid md:grid-cols-3 gap-12 reveal">
          {[
            { num: '18', label: 'Service', sub: '从聊天核心到生活服务' },
            { num: '15', label: 'AI Tool', sub: '让 AI 深度掌控设备' },
            { num: '∞', label: 'Plugin', sub: 'QuickJS 沙箱插件框架' },
          ].map((m, i) => (
            <div key={i} className="border-t border-warm-200 pt-8">
              <p className="heading-serif text-6xl md:text-7xl text-warm-900 mb-2">{m.num}</p>
              <p className="text-sm tracking-[0.15em] uppercase text-warm-400 mb-3">{m.label}</p>
              <p className="text-warm-500 text-sm">{m.sub}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ─── Screenshots ─── */
function Screenshots() {
  return (
    <section className="min-h-screen flex items-center justify-center bg-cream-200">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 text-center">
        <p className="text-xs tracking-[0.3em] uppercase text-warm-400 mb-4">Preview</p>
        <h2 className="heading-serif text-5xl md:text-6xl text-warm-900 mb-16">看看橘瓣</h2>
        <div className="flex justify-center gap-6">
          <img src="/img/chat.png" alt="" className="h-72 md:h-96 rounded shadow-lg" />
          <img src="/img/desktop.png" alt="" className="hidden md:block h-72 md:h-96 rounded shadow-lg" />
          <img src="/img/assistants.png" alt="" className="h-72 md:h-96 rounded shadow-lg" />
        </div>
      </div>
    </section>
  )
}

/* ─── Download ─── */
function Download() {
  const ref = useReveal()
  return (
    <section ref={ref} className="min-h-screen flex items-center justify-center bg-warm-900 text-cream-200">
      <div className="max-w-2xl mx-auto px-6 md:px-12 text-center reveal">
        <img src="/icon.png" alt="" className="w-16 h-16 rounded mx-auto mb-12" />
        <h2 className="heading-serif text-5xl md:text-7xl mb-4">开始使用<span className="text-accent-400">橘瓣</span></h2>
        <p className="text-warm-400 mb-12">让 AI 不止活在对话框里</p>
        <a href={QQ_GROUP_LINK} target="_blank" rel="noopener noreferrer"
          className="inline-block text-sm tracking-[0.15em] uppercase bg-accent-500 text-white px-10 py-4 hover:bg-accent-600 transition-colors duration-300">
          加入 QQ 群
        </a>
        <p className="text-warm-500 text-sm mt-6 font-mono tracking-wider">{QQ_GROUP}</p>
        <div className="mt-8 flex items-center justify-center gap-8 text-xs text-warm-500">
          <span>Android 8.0+</span>
          <span>·</span>
          <span>开源免费</span>
          <span>·</span>
          <span>Apache 2.0</span>
        </div>
      </div>
    </section>
  )
}

/* ─── Footer ─── */
function Footer() {
  return (
    <footer className="bg-warm-900 text-warm-500 border-t border-warm-800 py-10">
      <div className="max-w-screen-xl mx-auto px-6 md:px-12 flex flex-col md:flex-row items-center justify-between gap-4">
        <span className="text-xs">橘瓣 OrangeChat · Apache License 2.0 · 致谢 RikkaHub</span>
        <div className="flex gap-6 text-xs">
          <a href="https://github.com/tdevid523-bot/orangechat" target="_blank" rel="noopener noreferrer" className="hover:text-cream-200 transition-colors">GitHub</a>
          <a href={QQ_GROUP_LINK} target="_blank" rel="noopener noreferrer" className="hover:text-cream-200 transition-colors">QQ 群</a>
          <a href="https://github.com/rikkahub/rikkahub" target="_blank" rel="noopener noreferrer" className="hover:text-cream-200 transition-colors">RikkaHub</a>
        </div>
      </div>
    </footer>
  )
}

/* ─── App ─── */
export default function App() {
  return (
    <div className="bg-cream-100 text-warm-900">
      <Navbar />
      <Hero />
      <Manifesto />
      <Features />
      <Tools />
      <Architecture />
      <Screenshots />
      <Download />
      <Footer />
    </div>
  )
}