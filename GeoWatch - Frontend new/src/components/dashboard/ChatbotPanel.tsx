import { useState, useEffect, useRef } from 'react'
import api from '../../services/api'
import { Mic, Send, Bot, User, Loader2 } from 'lucide-react'

// Define types for Web Speech API since they aren't built-in strictly everywhere
declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

interface Message {
  id: string
  text: string
  sender: 'user' | 'bot'
}

interface ChatbotPanelProps {
  eventId: string
}

const languages = [
  { code: 'English', label: 'English', speechCode: 'en-IN' },
  { code: 'Hindi', label: 'Hindi', speechCode: 'hi-IN' },
  { code: 'Marathi', label: 'Marathi', speechCode: 'mr-IN' }
]

export default function ChatbotPanel({ eventId }: ChatbotPanelProps) {
  const [messages, setMessages] = useState<Message[]>([
    { id: '1', text: 'Hello! I am your Event Intelligence Assistant. How can I help you monitor this event?', sender: 'bot' }
  ])
  const [input, setInput] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isListening, setIsListening] = useState(false)
  const [selectedLanguage, setSelectedLanguage] = useState(languages[0])
  
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const recognitionRef = useRef<any>(null)

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    // Initialize Web Speech API
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    if (SpeechRecognition) {
      const recognition = new SpeechRecognition()
      recognition.continuous = false
      recognition.interimResults = false
      
      recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript
        setInput(prev => prev + (prev ? ' ' : '') + transcript)
        setIsListening(false)
      }
      
      recognition.onerror = (event: any) => {
        console.error("Speech recognition error", event.error)
        setIsListening(false)
      }
      
      recognition.onend = () => {
        setIsListening(false)
      }
      
      recognitionRef.current = recognition
    }
  }, [])

  useEffect(() => {
    if (recognitionRef.current) {
      recognitionRef.current.lang = selectedLanguage.speechCode
    }
  }, [selectedLanguage])

  const toggleListen = () => {
    if (!recognitionRef.current) {
      alert("Speech recognition is not supported in this browser.")
      return
    }
    
    if (isListening) {
      recognitionRef.current.stop()
      setIsListening(false)
    } else {
      try {
        recognitionRef.current.start()
        setIsListening(true)
      } catch (e) {
        console.error(e)
      }
    }
  }

  const sendMessage = async () => {
    if (!input.trim() || isLoading) return
    
    const userMsg = input.trim()
    setInput('')
    
    const newMessage: Message = { id: Date.now().toString(), text: userMsg, sender: 'user' }
    setMessages(prev => [...prev, newMessage])
    setIsLoading(true)
    
    try {
      const response = await api.post('/admin/chat', {
        eventId: parseInt(eventId, 10),
        message: userMsg,
        language: selectedLanguage.code
      })
      
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        text: response.data.answer || "I received an empty response.",
        sender: 'bot'
      }])
    } catch (error) {
      console.error("Chat error:", error)
      setMessages(prev => [...prev, {
        id: (Date.now() + 1).toString(),
        text: "Sorry, I encountered an error communicating with the server.",
        sender: 'bot'
      }])
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex flex-col h-[34rem] rounded-2xl border border-mid/40 bg-deep/50 shadow-[0_8px_32px_rgba(0,0,0,0.25)] backdrop-blur-xl overflow-hidden glass-hover">
      {/* Header */}
      <div className="p-4 border-b border-mid/30 flex justify-between items-center bg-surface/30">
        <div className="flex items-center gap-2">
          <Bot className="h-5 w-5 text-cyan-400" />
          <h2 className="font-semibold text-[#ccd0cf]">Event Intelligence</h2>
        </div>
        <select 
          value={selectedLanguage.code}
          onChange={(e) => setSelectedLanguage(languages.find(l => l.code === e.target.value) || languages[0])}
          className="bg-deep border border-mid/40 text-xs rounded-lg py-1.5 px-2.5 text-[#ccd0cf] outline-none hover:border-light/40 transition"
        >
          {languages.map(lang => (
            <option key={lang.code} value={lang.code}>{lang.label}</option>
          ))}
        </select>
      </div>
      
      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth">
        {messages.map(msg => (
          <div key={msg.id} className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[85%] rounded-xl p-3.5 text-sm shadow-sm backdrop-blur-sm border ${
              msg.sender === 'user' 
                ? 'bg-cyan-600/90 text-white border-cyan-500/50 rounded-br-sm' 
                : 'bg-surface/80 text-[#ccd0cf] border-mid/40 rounded-bl-sm'
            }`}>
              <div className="flex items-center gap-2 mb-1.5 opacity-80 text-[10px] uppercase font-bold tracking-wider">
                {msg.sender === 'user' ? <User className="h-3 w-3" /> : <Bot className="h-3 w-3 text-cyan-400" />}
                {msg.sender}
              </div>
              <p className="whitespace-pre-wrap leading-relaxed">{msg.text}</p>
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="flex justify-start">
            <div className="bg-surface/80 border border-mid/40 rounded-xl rounded-bl-sm p-3.5 text-muted flex items-center gap-3 backdrop-blur-sm">
              <Loader2 className="h-4 w-4 animate-spin text-cyan-400" />
              <span className="text-sm font-medium animate-pulse">Analyzing event data...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>
      
      {/* Input */}
      <div className="p-4 border-t border-mid/30 bg-surface/40">
        <div className="flex gap-2">
          <button 
            type="button"
            onClick={toggleListen}
            className={`flex-shrink-0 p-3 rounded-xl border transition-all duration-200 shadow-sm ${
              isListening 
                ? 'bg-rose-500/20 border-rose-500/50 text-rose-400 shadow-rose-500/20' 
                : 'bg-deep/80 border-mid/50 text-muted hover:text-[#ccd0cf] hover:border-light/40 hover:bg-mid/40'
            }`}
            title={isListening ? "Listening... click to stop" : "Speak (Voice to Text)"}
          >
            <Mic className={`h-5 w-5 ${isListening ? 'animate-pulse' : ''}`} />
          </button>
          
          <input 
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
            placeholder="Ask about the current event..."
            className="glass-input flex-1"
            disabled={isLoading}
          />
          
          <button
            onClick={sendMessage}
            disabled={isLoading || !input.trim()}
            className="flex-shrink-0 p-3 rounded-xl bg-[#ccd0cf] text-[#06141b] shadow-lg shadow-black/20 disabled:opacity-50 disabled:cursor-not-allowed transition hover:brightness-110 active:scale-95"
          >
            <Send className="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>
  )
}
