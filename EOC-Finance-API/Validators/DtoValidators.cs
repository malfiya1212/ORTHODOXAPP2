using FluentValidation;
using EOC.Finance.API.DTOs;

namespace EOC.Finance.API.Validators
{
    public class LoginDtoValidator : AbstractValidator<LoginDto>
    {
        public LoginDtoValidator()
        {
            RuleFor(x => x.Email)
                .NotEmpty().WithMessage("Email is required.")
                .EmailAddress().WithMessage("Invalid email format.");

            RuleFor(x => x.Password)
                .NotEmpty().WithMessage("Password is required.")
                .MinimumLength(6).WithMessage("Password must be at least 6 characters.");
        }
    }

    public class IncomeDtoValidator : AbstractValidator<IncomeDto>
    {
        public IncomeDtoValidator()
        {
            RuleFor(x => x.Amount)
                .GreaterThan(0).WithMessage("Income amount must be greater than zero.")
                .LessThanOrEqualTo(10000000).WithMessage("Transaction exceeds safety limit of 10M ETB.");

            RuleFor(x => x.Source)
                .NotEmpty().WithMessage("Income source is required.")
                .MaximumLength(200).WithMessage("Source description too long.");

            RuleFor(x => x.ChurchId)
                .NotEmpty().WithMessage("Church assignment is required.");
        }
    }

    public class ExpenseDtoValidator : AbstractValidator<ExpenseDto>
    {
        public ExpenseDtoValidator()
        {
            RuleFor(x => x.Amount)
                .GreaterThan(0).WithMessage("Expense amount must be greater than zero.");

            RuleFor(x => x.Recipient)
                .NotEmpty().WithMessage("Recipient name is required.");

            RuleFor(x => x.Category)
                .NotEmpty().WithMessage("Expense category is required.");
        }
    }
}
