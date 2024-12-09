package com.stb.expensetrackerapp;

public class QuotesUtility {
    private String[] quotes = {
            "Track Your Spending: Monitor all expenses to understand where your money goes.",
            "Create a Budget: Develop a realistic budget based on your income and expenses.",
            "Use the Envelope System: Allocate cash for different spending categories.",
            "Set Savings Goals: Define clear savings goals to stay motivated.",
            "Automate Savings: Set up automatic transfers to your savings account each month.",
            "Plan Meals Ahead: Create a weekly meal plan to avoid impulse grocery purchases.",
            "Make a Shopping List: Stick to a list when grocery shopping to reduce unnecessary spending.",
            "Cook at Home: Prepare meals at home instead of dining out to save money.",
            "Limit Eating Out: Reduce the number of times you eat out each month.",
            "Use Coupons and Discounts: Always look for coupons before making a purchase.",
            "Understand Fixed vs. Variable Expenses: Differentiate between fixed (rent, utilities) and variable (groceries, entertainment) expenses.",
            "Review Subscriptions Regularly: Cancel any subscriptions or memberships you no longer use.",
            "Plan for Irregular Expenses: Set aside money for irregular expenses like car maintenance or insurance premiums.",
            "Prioritize Needs Over Wants: Focus on essential expenses first before discretionary spending.",
            "Use Budgeting Apps: Utilize apps that help track and categorize your spending automatically.",
            "Adjust Your Budget Regularly: Review and adjust your budget as your financial situation changes.",
            "Implement the 50/30/20 Rule: Allocate 50% of income to needs, 30% to wants, and 20% to savings.",
            "Create a Zero-Based Budget: Ensure every dollar has a purpose, with income minus expenses equaling zero.",
            "Set Up an Emergency Fund: Aim to save three to six months' worth of living expenses for emergencies.",
            "Review Your Financial Goals Monthly: Regularly assess your progress towards financial goals.",
            "Use the 24-Hour Rule: Wait 24 hours before making non-essential purchases to avoid impulse buys.",
            "Save Your Spare Change: Collect coins and small bills in a jar for unexpected savings.",
            "Take Advantage of Cashback Offers: Use credit cards that offer cashback on purchases you would make anyway.",
            "Buy in Bulk: Purchase non-perishable items in bulk to save money in the long run.",
            "Shop Seasonal Sales: Buy clothing and holiday items during off-season sales for better deals.",
            "Negotiate Bills: Contact service providers to negotiate lower rates on bills like cable or insurance.",
            "Reduce Energy Consumption: Implement energy-saving practices at home to lower utility bills.",
            "Limit Luxuries: Cut back on luxury items or services that are not essential.",
            "Find Free Entertainment Options: Look for free local events or activities instead of costly outings.",
            "Use Public Transportation: Save on gas and parking by using public transport when possible.",
            "Brown Bag Your Lunch: Bring homemade lunches to work instead of buying food out.",
            "DIY When Possible: Learn basic home repairs or cooking skills instead of hiring help or eating out frequently.",
            "Use Library Resources: Borrow books, movies, and games from the library instead of purchasing them.",
            "Participate in Community Events: Engage in free community events for entertainment rather than costly outings.",
            "Make Gifts Instead of Buying Them: Create homemade gifts for friends and family instead of purchasing expensive items.",
            "Review Your Credit Report Annually: Check your credit report for errors and understand your credit score better.",
            "Start Investing Early: Begin investing as soon as possible; even small amounts can grow significantly over time through compounding interest.",
            "Consider Side Hustles for Extra Income: Explore freelance work or part-time jobs that align with your skills and interests for additional income.",
            "Stay Informed About Financial News: Keep up with economic trends that could affect your financial situation or investments.",
            "Seek Professional Advice When Needed: Consult with a financial advisor if you're unsure about investment strategies or budgeting.",
            "Stay Disciplined with Savings Goals: Treat savings like a bill that must be paid each month; prioritize it in your budget.",
            "Limit Shopping Trips to Reduce Temptation: Avoid frequent trips to stores where you're likely to overspend on non-essentials.",
            "Celebrate Small Wins in Saving and Budgeting: Acknowledge milestones in your savings journey, no matter how small they may seem.",
            "Be Patient with Financial Goals: Understand that achieving significant financial goals takes time and persistence.",
            "Reflect on Your Financial Journey Regularly: Take time each month to reflect on what worked well and what could be improved in your financial habits."
    };

    // Method to get all quotes
    public String[] getQuotes() {
        return quotes;
    }

    public String getQuote(int index) {
        if (index < 0 || index >= quotes.length) {
            return "Invalid index";
        }
        return quotes[index];
    }

    // Method to get a random quote
    public String getRandomQuote() {
        int index = (int) (Math.random() * quotes.length);
        return quotes[index];
    }
}
